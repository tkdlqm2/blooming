package com.bloominggrace.governance.shared.infrastructure.service.ethereum;

import com.bloominggrace.governance.blockchain.infrastructure.service.ethereum.EthereumBlockchainClient;
import com.bloominggrace.governance.shared.domain.service.RawTransactionBuilder;
import com.bloominggrace.governance.blockchain.application.service.BlockchainClientFactory;
import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.shared.domain.model.BlockchainMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.Arrays;
import java.util.Collections;

// Web3j imports
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.TypeReference;

/**
 * 이더리움 네트워크용 RawTransaction 생성기
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EthereumRawTransactionBuilder implements RawTransactionBuilder {

    private final BlockchainClientFactory blockchainClientFactory;

    // 블록 생성 평균 시간 (초) — Ethereum 메인넷 기준
    private static final long BLOCK_TIME_SECONDS = 12;

    /**
     * 통합 RawTransaction 생성 메서드
     * ETH 전송과 ERC-20 토큰 전송을 자동 감지하여 처리
     *
     * @param data 트랜잭션 데이터
     *   - fromAddress: 발신자 주소 (필수)
     *   - toAddress: 수신자 주소 (필수)
     *   - amount: 전송할 금액 (필수)
     *   - tokenAddress: 토큰 컨트랙트 주소 (ERC-20인 경우)
     *   - nonce: 트랜잭션 nonce (선택, 없으면 자동 조회)
     *   - gasPrice: 가스 가격 (선택, 없으면 기본값 사용)
     *   - gasLimit: 가스 한도 (선택, 없으면 자동 추정)
     * @return JSON 형태의 RawTransaction
     */
    @Override
    public String createRawTransaction(Map<String, String> data) {
        try {
            // 1. 필수 파라미터 검증 및 추출
            TransactionParams params = validateAndExtractParams(data);

            // 2. 트랜잭션 타입 결정 (ETH vs ERC-20)
            TransactionType txType = determineTransactionType(params);

            log.info("[EthereumRawTransactionBuilder] Creating {} transaction - From: {}, To: {}, Amount: {}",
                    txType, params.getFromAddress(), params.getToAddress(), params.getAmount());

            // 3. nonce 처리
            String resolvedNonce = resolveNonce(params.getFromAddress(), params.getNonce());

            // 4. 트랜잭션별 데이터 생성
            TransactionData txData = createTransactionData(txType, params);

            // 5. 가스 추정 및 설정
            GasConfig gasConfig = estimateAndConfigureGas(params, txData, txType);

            // 6. 최종 JSON 생성
            String rawTransactionJson = buildRawTransactionJson(
                    params, txData, gasConfig, resolvedNonce
            );

            log.info("[EthereumRawTransactionBuilder] Created {} RawTransaction successfully", txType);
            log.debug("[EthereumRawTransactionBuilder] RawTransaction JSON: {}", rawTransactionJson);

            return rawTransactionJson;

        } catch (Exception e) {
            log.error("[EthereumRawTransactionBuilder] Failed to create RawTransaction", e);
            throw new RuntimeException("Failed to create RawTransaction: " + e.getMessage(), e);
        }
    }

    @Override
    public String createProposalCreationRawTransaction(
            UUID proposalId,
            String title,
            String description,
            String walletAddress,
            BigDecimal proposalFee,
            LocalDateTime votingStartDate,
            LocalDateTime votingEndDate,
            BigDecimal requiredQuorum,
            String nonce
    ) {
        try {
            log.info("[EthereumRawTransactionBuilder] Creating Proposal Creation RawTransaction - ProposalId: {}, Title: {}, Wallet: {}",
                    proposalId, title, walletAddress);

            // 1. nonce가 제공되지 않은 경우 블록체인에서 조회
            if (nonce == null || nonce.isEmpty()) {
                BlockchainClient blockchainClient = blockchainClientFactory.getClient(NetworkType.ETHEREUM);
                nonce = blockchainClient.getNonce(walletAddress);
                log.info("[EthereumRawTransactionBuilder] Got nonce for {}: {}", walletAddress, nonce);
            }

            // 2. 현재 블록 정보 가져오기
            BlockchainClient blockchainClient = blockchainClientFactory.getClient(NetworkType.ETHEREUM);
            BigInteger currentBlock = new BigInteger(blockchainClient.getLatestBlockNumber());
            Long currentBlockTimestamp = blockchainClient.getBlockTimestamp(null); // null = latest block

            if (currentBlockTimestamp == null) {
                throw new RuntimeException("Failed to get current block timestamp");
            }

            long currentTimestamp = currentBlockTimestamp;
            log.info("[EthereumRawTransactionBuilder] Current block: {}, Current timestamp: {}", currentBlock, currentTimestamp);

            // 3. 투표 시작/종료 시간 설정 (LocalDateTime → epoch seconds)
            long votingStartTimestamp = votingStartDate.toEpochSecond(ZoneOffset.UTC);
            long votingEndTimestamp = votingEndDate.toEpochSecond(ZoneOffset.UTC);

            // 🔧 디버깅 로그 추가
            log.info("=== 시간 변환 디버깅 ===");
            log.info("votingStartDate: {}", votingStartDate);
            log.info("votingEndDate: {}", votingEndDate);
            log.info("votingStartTimestamp: {} ({})", votingStartTimestamp, java.time.Instant.ofEpochSecond(votingStartTimestamp));
            log.info("votingEndTimestamp: {} ({})", votingEndTimestamp, java.time.Instant.ofEpochSecond(votingEndTimestamp));
            log.info("currentTimestamp: {} ({})", currentTimestamp, java.time.Instant.ofEpochSecond(currentTimestamp));

            // 4. epoch seconds → 블록 번호 변환
            BigInteger startBlock = convertDateTimeToBlock(votingStartTimestamp, currentTimestamp, currentBlock);
            BigInteger endBlock = startBlock.add(BigInteger.valueOf(50500));

            log.info("=== 블록 변환 결과 ===");
            log.info("Start block: {}", startBlock);
            log.info("End block: {}", endBlock);
            log.info("Voting duration in blocks: {}", endBlock.subtract(startBlock));

            // 5. 거버넌스 컨트랙트 주소 가져오기
            String governanceContractAddress = BlockchainMetadata.Ethereum.GOVERNANCE_CONTRACT_ADDRESS;

            // 6. 실제 거버넌스 컨트랙트의 propose() 함수 데이터 생성 (블록 번호 포함)
            String functionData = createProposeFunctionData(title, description, startBlock.add(BigInteger.valueOf(6)), endBlock);

            // 7. RawTransaction 생성
            BigInteger gasPrice = BlockchainMetadata.Ethereum.GAS_PRICE;
            BigInteger gasLimit = BlockchainMetadata.Ethereum.PROPOSAL_CREATION_GAS_LIMIT;
            BigInteger value = BigInteger.ZERO; // propose() 함수는 value가 0

            // 8. RawTransaction을 JSON 형태로 반환 (기존 인터페이스 유지)
            String rawTransactionJson = String.format(
                    "{\"fromAddress\":\"%s\",\"toAddress\":\"%s\",\"data\":\"%s\",\"value\":\"%s\",\"nonce\":\"%s\",\"gasPrice\":\"%s\",\"gasLimit\":\"%s\"}",
                    walletAddress, governanceContractAddress, functionData, value.toString(), nonce, gasPrice.toString(), gasLimit.toString()
            );

            log.info("[EthereumRawTransactionBuilder] Created Proposal Creation RawTransaction JSON: {}", rawTransactionJson);
            return rawTransactionJson;

        } catch (Exception e) {
            log.error("[EthereumRawTransactionBuilder] Failed to create Proposal Creation RawTransaction", e);
            throw new RuntimeException("Failed to create Proposal Creation RawTransaction: " + e.getMessage(), e);
        }
    }


    @Override
    public String createVoteRawTransaction(
            BigInteger proposalCount,
            UUID proposalId,
            String walletAddress,
            String voteType,
            String reason,
            BigDecimal votingPower,
            String nonce
    ) {
        try {
            log.info("[EthereumRawTransactionBuilder] Creating Vote RawTransaction - ProposalId: {}, VoteType: {}, Wallet: {}",
                    proposalId, voteType, walletAddress);

            // 1. nonce가 제공되지 않은 경우 블록체인에서 조회
            if (nonce == null || nonce.isEmpty()) {
                BlockchainClient blockchainClient = blockchainClientFactory.getClient(NetworkType.ETHEREUM);
                nonce = blockchainClient.getNonce(walletAddress);
                log.info("[EthereumRawTransactionBuilder] Got nonce for {}: {}", walletAddress, nonce);
            }

            // 2. 거버넌스 컨트랙트 주소 가져오기
            String governanceContractAddress = BlockchainMetadata.Ethereum.GOVERNANCE_CONTRACT_ADDRESS;

            // 3. 투표 함수 데이터 생성 (vote 함수 호출)
            String functionData = createVoteFunctionData(proposalCount, voteType);

            // 4. RawTransaction 생성
            BigInteger gasPrice = BlockchainMetadata.Ethereum.GAS_PRICE;
            BigInteger gasLimit = BlockchainMetadata.Ethereum.VOTE_GAS_LIMIT;
            BigInteger value = BigInteger.ZERO; // 투표는 value가 0

            // 5. JSON 형태로 반환
            String rawTransactionJson = String.format(
                    "{\"fromAddress\":\"%s\",\"toAddress\":\"%s\",\"data\":\"%s\",\"value\":\"%s\",\"nonce\":\"%s\",\"gasPrice\":\"%s\",\"gasLimit\":\"%s\"}",
                    walletAddress, governanceContractAddress, functionData, value.toString(), nonce, gasPrice.toString(), gasLimit.toString()
            );

            log.info("[EthereumRawTransactionBuilder] Created Vote RawTransaction: {}", rawTransactionJson);
            return rawTransactionJson;

        } catch (Exception e) {
            log.error("[EthereumRawTransactionBuilder] Failed to create Vote RawTransaction", e);
            throw new RuntimeException("Failed to create Vote RawTransaction: " + e.getMessage(), e);
        }
    }

    /**
     * 투표권 위임을 위한 RawTransaction JSON 생성
     *
     * @param delegatorWalletAddress 위임하는 지갑 주소
     * @param delegateeWalletAddress 위임받는 지갑 주소
     * @param networkType 네트워크 타입
     * @return RawTransaction JSON 문자열
     */
    @Override
    public String createDelegationRawTransaction(
            String delegatorWalletAddress,
            String delegateeWalletAddress,
            NetworkType networkType) {

        try {
            log.info("[EthereumRawTransactionBuilder] Creating Delegation RawTransaction - Delegator: {}, Delegatee: {}, Network: {}",
                    delegatorWalletAddress, delegateeWalletAddress, networkType);

            // 1. nonce 조회
            BlockchainClient blockchainClient = blockchainClientFactory.getClient(NetworkType.ETHEREUM);
            String nonce = blockchainClient.getNonce(delegatorWalletAddress);
            log.info("[EthereumRawTransactionBuilder] Got nonce for {}: {}", delegatorWalletAddress, nonce);

            // 2. 거버넌스 토큰 컨트랙트 주소 가져오기
            String tokenContractAddress = BlockchainMetadata.Ethereum.ERC20_CONTRACT_ADDRESS;

            // 3. 위임 함수 데이터 생성
            String functionData = createDelegateFunctionData(delegateeWalletAddress);

            // 4. RawTransaction 생성
            BigInteger gasPrice = BlockchainMetadata.Ethereum.GAS_PRICE;
            BigInteger gasLimit = BlockchainMetadata.Ethereum.GAS_LIMIT;
            BigInteger value = BigInteger.ZERO; // 위임은 value가 0

            // 5. RawTransaction을 JSON 형태로 반환
            String rawTransactionJson = String.format(
                    "{\"fromAddress\":\"%s\",\"toAddress\":\"%s\",\"data\":\"%s\",\"value\":\"%s\",\"nonce\":\"%s\",\"gasPrice\":\"%s\",\"gasLimit\":\"%s\"}",
                    delegatorWalletAddress, tokenContractAddress, functionData, value.toString(), nonce, gasPrice.toString(), gasLimit.toString()
            );

            log.info("[EthereumRawTransactionBuilder] Created Delegation RawTransaction JSON: {}", rawTransactionJson);
            return rawTransactionJson;

        } catch (Exception e) {
            log.error("[EthereumRawTransactionBuilder] Failed to create Delegation RawTransaction", e);
            throw new RuntimeException("Failed to create Delegation RawTransaction: " + e.getMessage(), e);
        }
    }

    /**
     * 투표 함수 데이터 생성
     */
    private String createVoteFunctionData(
            BigInteger proposalCount,
            String voteType
    ) {
        try {
            // voteType을 숫자로 변환 (0: AGAINST, 1: FOR, 2: ABSTAIN)
            int voteTypeNumber;
            switch (voteType.toUpperCase()) {
                case "NO":
                case "AGAINST":
                    voteTypeNumber = 0;
                    break;
                case "YES":
                case "FOR":
                    voteTypeNumber = 1;
                    break;
                case "ABSTAIN":
                    voteTypeNumber = 2;
                    break;
                default:
                    voteTypeNumber = 0;
            }

            Function voteFunction = new Function(
                    "vote",
                    Arrays.asList(
                            new Uint256(proposalCount),
                            new Uint8(BigInteger.valueOf(voteTypeNumber))
                    ),
                    Collections.emptyList()
            );

            return FunctionEncoder.encode(voteFunction);

        } catch (Exception e) {
            log.error("[EthereumRawTransactionBuilder] Failed to create vote function data", e);
            throw new RuntimeException("Failed to create vote function data: " + e.getMessage(), e);
        }
    }

    /**
     * 파라미터 검증 및 추출
     */
    private TransactionParams validateAndExtractParams(Map<String, String> data) {
        String fromAddress = data.get("fromAddress");
        String toAddress = data.get("toAddress");
        String amount = data.get("amount");

        if (fromAddress == null || fromAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("fromAddress is required");
        }
        if (toAddress == null || toAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("toAddress is required");
        }
        if (amount == null || amount.trim().isEmpty()) {
            throw new IllegalArgumentException("amount is required");
        }

        return TransactionParams.builder()
                .fromAddress(fromAddress.trim())
                .toAddress(toAddress.trim())
                .amount(amount.trim())
                .tokenAddress(data.get("tokenAddress"))
                .nonce(data.get("nonce"))
                .gasPrice(data.get("gasPrice"))
                .gasLimit(data.get("gasLimit"))
                .build();
    }

    /**
     * 트랜잭션 타입 결정
     */
    private TransactionType determineTransactionType(TransactionParams params) {
        // tokenAddress가 있으면 ERC-20, 없으면 ETH 전송
        return (params.getTokenAddress() != null && !params.getTokenAddress().trim().isEmpty())
                ? TransactionType.ERC20_TRANSFER
                : TransactionType.ETH_TRANSFER;
    }

    /**
     * nonce 처리 (제공되지 않은 경우 블록체인에서 조회)
     */
    private String resolveNonce(String fromAddress, String providedNonce) {
        if (providedNonce != null && !providedNonce.trim().isEmpty()) {
            return providedNonce.trim();
        }

        try {
            BlockchainClient blockchainClient = blockchainClientFactory.getClient(NetworkType.ETHEREUM);
            String nonce = blockchainClient.getNonce(fromAddress);
            log.info("[EthereumRawTransactionBuilder] Retrieved nonce for {}: {}", fromAddress, nonce);
            return nonce;
        } catch (Exception e) {
            log.error("[EthereumRawTransactionBuilder] Failed to get nonce for {}", fromAddress, e);
            throw new RuntimeException("Failed to get nonce: " + e.getMessage(), e);
        }
    }

    /**
     * 트랜잭션별 데이터 생성
     */
    private TransactionData createTransactionData(TransactionType txType, TransactionParams params) {
        switch (txType) {
            case ETH_TRANSFER:
                return createEthTransferData(params);
            case ERC20_TRANSFER:
                return createErc20TransferData(params);
            default:
                throw new IllegalArgumentException("Unsupported transaction type: " + txType);
        }
    }

    /**
     * ETH 전송 데이터 생성
     */
    private TransactionData createEthTransferData(TransactionParams params) {
        // ETH 전송 시 Wei 단위로 변환
        BigDecimal amountInWei = new BigDecimal(params.getAmount())
                .multiply(BigDecimal.valueOf(1e18));

        return TransactionData.builder()
                .toAddress(params.getToAddress())
                .value(amountInWei.toBigInteger())
                .data("0x") // ETH 전송은 빈 데이터
                .build();
    }

    /**
     * ERC-20 토큰 전송 데이터 생성
     */
    private TransactionData createErc20TransferData(TransactionParams params) {
        // ERC-20 transfer 함수 데이터 생성
        String functionData = createERC20TransferFunctionData(
                params.getToAddress(),
                new BigDecimal(params.getAmount())
        );

        return TransactionData.builder()
                .toAddress(params.getTokenAddress()) // 토큰 컨트랙트 주소로 전송
                .value(BigInteger.ZERO) // ERC-20은 value를 0으로 설정
                .data(functionData) // transfer 함수 호출 데이터
                .build();
    }

    /**
     * ERC-20 transfer 함수 데이터 생성 (수동 방식)
     */
    private String createERC20TransferFunctionData(String toAddress, BigDecimal amount) {
        try {
            // ERC-20 transfer(address,uint256) 함수 시그니처: 0xa9059cbb
            String methodSignature = "0xc2c1a19c";

            // 주소 패딩 (32바이트, 64글자)
            String cleanAddress = toAddress.startsWith("0x") ? toAddress.substring(2) : toAddress;
            String paddedAddress = String.format("%64s", cleanAddress.toLowerCase()).replace(' ', '0');

            // 금액을 18 decimal로 변환 후 패딩
            BigInteger amountWei = amount.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            String paddedAmount = String.format("%64s", amountWei.toString(16)).replace(' ', '0');

            String functionData = methodSignature + paddedAddress + paddedAmount;

            log.debug("[EthereumRawTransactionBuilder] Created ERC-20 function data: {}", functionData);
            return functionData;

        } catch (Exception e) {
            log.error("[EthereumRawTransactionBuilder] Failed to create ERC-20 function data", e);
            throw new RuntimeException("Failed to create ERC-20 function data: " + e.getMessage(), e);
        }
    }

    /**
     * 가스 추정 및 설정
     */
    private GasConfig estimateAndConfigureGas(TransactionParams params, TransactionData txData, TransactionType txType) {
        BigInteger gasPrice = BlockchainMetadata.Ethereum.GAS_PRICE;
        BigInteger gasLimit = resolveGasLimit(params, txData, txType);

        return GasConfig.builder()
                .gasPrice(gasPrice)
                .gasLimit(gasLimit)
                .build();
    }

    /**
     * 가스 가격 결정
     */
    private BigInteger resolveGasPrice(String providedGasPrice) {
        if (providedGasPrice != null && !providedGasPrice.trim().isEmpty()) {
            return new BigInteger(providedGasPrice.trim());
        }

        // 기본 가스 가격 사용 또는 네트워크에서 조회
        return BlockchainMetadata.Ethereum.GAS_PRICE;
    }

    /**
     * 가스 한도 결정
     */
    private BigInteger resolveGasLimit(TransactionParams params, TransactionData txData, TransactionType txType) {
        if(txType == TransactionType.ERC20_TRANSFER) {
            return BlockchainMetadata.Ethereum.TRANSFER_DELEGATE_GAS_LIMIT;
        } else {
            return BlockchainMetadata.Ethereum.GAS_LIMIT;
        }
    }


    /**
     * 최종 RawTransaction JSON 생성
     */
    private String buildRawTransactionJson(TransactionParams params, TransactionData txData,
                                           GasConfig gasConfig, String nonce) {
        return String.format(
                "{\"fromAddress\":\"%s\",\"toAddress\":\"%s\",\"value\":\"%s\",\"data\":\"%s\"," +
                        "\"nonce\":\"%s\",\"gasPrice\":\"%s\",\"gasLimit\":\"%s\",\"amount\":\"%s\"," +
                        "\"tokenAddress\":\"%s\"}",
                params.getFromAddress(),
                txData.getToAddress(),
                txData.getValue().toString(),
                txData.getData(),
                nonce,
                gasConfig.getGasPrice().toString(),
                gasConfig.getGasLimit().toString(),
                params.getAmount(),
                params.getTokenAddress() != null ? params.getTokenAddress() : ""
        );
    }

    /**
     * delegate 함수 데이터 생성
     * delegate(address delegatee)
     * Method ID: 0x5c19a95c
     */
    private String createDelegateFunctionData(String delegateeWalletAddress) {
        try {
            // delegate 함수 시그니처: delegate(address delegatee)
            Function delegateFunction = new Function(
                    "delegate",
                    Arrays.asList(new Address(delegateeWalletAddress)),
                    Collections.emptyList()
            );

            String functionData = FunctionEncoder.encode(delegateFunction);
            
            log.info("=== 위임 함수 데이터 생성 완료 ===");
            log.info("Method ID: 0x5c19a95c");
            log.info("Delegatee: {}", delegateeWalletAddress);
            log.info("Function data: {}", functionData);
            
            return functionData;

        } catch (Exception e) {
            log.error("Failed to create delegate function data", e);
            throw new RuntimeException("Failed to create delegate function data: " + e.getMessage(), e);
        }
    }

    /**
     * 시간을 블록 번호로 변환
     */
    private static BigInteger convertDateTimeToBlock(long targetTimestamp, long currentTimestamp, BigInteger currentBlock) {
        // 🔧 BLOCK_TIME_SECONDS 상수 확인 필요!
        final long BLOCK_TIME_SECONDS = 12L; // 이더리움 평균 블록 시간 (12초)

        // 🔍 디버깅 로그
        log.info("=== convertDateTimeToBlock Debug ===");
        log.info("targetTimestamp: {} ({})", targetTimestamp, java.time.Instant.ofEpochSecond(targetTimestamp));
        log.info("currentTimestamp: {} ({})", currentTimestamp, java.time.Instant.ofEpochSecond(currentTimestamp));
        log.info("currentBlock: {}", currentBlock);
        log.info("BLOCK_TIME_SECONDS: {}", BLOCK_TIME_SECONDS);

        long timeDiff = targetTimestamp - currentTimestamp;
        log.info("timeDiff: {} seconds ({} days)", timeDiff, timeDiff / 86400.0);

        long blockDiff = timeDiff / BLOCK_TIME_SECONDS;
        log.info("blockDiff: {}", blockDiff);

        if (blockDiff < 0) {
            log.warn("Target time is in the past! Setting blockDiff to 0");
            blockDiff = 0; // 과거 시간 방지
        }

        BigInteger result = currentBlock.add(BigInteger.valueOf(blockDiff));
        log.info("result block: {}", result);

        // 🚨 비정상적인 결과 경고
        double estimatedDays = Math.abs(blockDiff * BLOCK_TIME_SECONDS) / 86400.0;
        if (estimatedDays > 365) {
            log.error("⚠️ ABNORMAL RESULT: {} blocks = {} days", blockDiff, estimatedDays);
            log.error("Check BLOCK_TIME_SECONDS constant!");
        }

        return result;
    }

    /**
     * 수정된 제안 함수 데이터 생성 (블록 번호 포함)
     */
    private String createProposeFunctionData(String title, String description, BigInteger startBlock, BigInteger endBlock) {
        try {
            // 실제 컨트랙트의 propose 함수 시그니처에 맞게 수정
            // 예: propose(string memory title, string memory description, uint256 startBlock, uint256 endBlock)

            Function proposeFunction = new Function(
                    "propose", // 또는 "createProposal"
                    Arrays.asList(
                            new Utf8String(title),
                            new Utf8String(description),
                            new Uint256(startBlock),
                            new Uint256(endBlock)
                    ),
                    Collections.emptyList()
            );

            String functionData = FunctionEncoder.encode(proposeFunction);

            log.info("=== 함수 데이터 생성 완료 ===");
            log.info("Title: {}", title);
            log.info("Description: {}", description);
            log.info("Start Block: {}", startBlock);
            log.info("End Block: {}", endBlock);
            log.info("Function data: {}", functionData);

            return functionData;

        } catch (Exception e) {
            log.error("Failed to create propose function data", e);
            throw new RuntimeException("Failed to create propose function data: " + e.getMessage(), e);
        }
    }
    // === 내부 데이터 클래스들 ===

    /**
     * 트랜잭션 타입 열거형
     */
    private enum TransactionType {
        ETH_TRANSFER,
        ERC20_TRANSFER
    }

    /**
     * 트랜잭션 파라미터 DTO
     */
    @Builder
    @Data
    private static class TransactionParams {
        private final String fromAddress;
        private final String toAddress;
        private final String amount;
        private final String tokenAddress;
        private final String nonce;
        private final String gasPrice;
        private final String gasLimit;
    }

    /**
     * 트랜잭션 데이터 DTO
     */
    @Builder
    @Data
    private static class TransactionData {
        private final String toAddress;
        private final BigInteger value;
        private final String data;
    }

    /**
     * 가스 설정 DTO
     */
    @Builder
    @Data
    private static class GasConfig {
        private final BigInteger gasPrice;
        private final BigInteger gasLimit;
    }
}