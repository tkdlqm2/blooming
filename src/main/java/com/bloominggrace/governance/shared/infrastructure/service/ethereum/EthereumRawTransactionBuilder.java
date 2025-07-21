package com.bloominggrace.governance.shared.infrastructure.service.ethereum;

import com.bloominggrace.governance.shared.domain.service.RawTransactionBuilder;
import com.bloominggrace.governance.blockchain.application.service.BlockchainClientFactory;
import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.shared.domain.model.BlockchainMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
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
import org.web3j.abi.TypeReference;
import org.web3j.crypto.RawTransaction;
import org.web3j.utils.Numeric;

/**
 * 이더리움 네트워크용 RawTransaction 생성기
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EthereumRawTransactionBuilder implements RawTransactionBuilder {
    
    private final BlockchainClientFactory blockchainClientFactory;
    
    @Override
    public String createRawTransaction(Map<String, String> data) {
        try {
            String fromAddress = data.get("fromAddress");
            String toAddress = data.get("toAddress");
            String tokenAddress = data.get("tokenAddress");
            String amount = data.get("amount");
            String nonce = data.get("nonce");
            
            log.info("[EthereumRawTransactionBuilder] Creating ERC20 RawTransaction - From: {}, To: {}, Token: {}, Amount: {}, Nonce: {}", 
                fromAddress, toAddress, tokenAddress, amount, nonce);
            
            // 1. nonce가 제공되지 않은 경우 블록체인에서 조회
            if (nonce == null || nonce.isEmpty()) {
                BlockchainClient blockchainClient = blockchainClientFactory.getClient(NetworkType.ETHEREUM);
                nonce = blockchainClient.getNonce(fromAddress);
                log.info("[EthereumRawTransactionBuilder] Got nonce for {}: {}", fromAddress, nonce);
            }
            
            // 2. ERC-20 transfer 함수 데이터 생성
            String functionData = createERC20TransferFunctionData(toAddress, new BigDecimal(amount));
            
            // 3. 가스 한도 추정 (블록체인에서 실제 가스 사용량 조회)
            BlockchainClient blockchainClient = blockchainClientFactory.getClient(NetworkType.ETHEREUM);
            String estimatedGas = blockchainClient.estimateGas(fromAddress, tokenAddress, functionData);
            BigInteger gasLimit;
            
            if (estimatedGas != null && !estimatedGas.isEmpty()) {
                gasLimit = new BigInteger(estimatedGas).multiply(BigInteger.valueOf(120L)).divide(BigInteger.valueOf(100L)); // 20% 버퍼 추가
                log.info("[EthereumRawTransactionBuilder] Estimated gas: {}, Using gas limit: {}", estimatedGas, gasLimit);
            } else {
                gasLimit = BlockchainMetadata.Ethereum.ERC20_TRANSFER_GAS_LIMIT; // 기본값 사용
                log.info("[EthereumRawTransactionBuilder] Using default gas limit: {}", gasLimit);
            }
            
            // 4. RawTransaction 생성
            BigInteger nonceBigInt = new BigInteger(nonce);
            BigInteger gasPrice = BlockchainMetadata.Ethereum.GAS_PRICE;
            BigInteger value = BigInteger.ZERO; // ERC20 전송은 value를 0으로 설정
            
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonceBigInt,
                gasPrice,
                gasLimit,
                tokenAddress, // 토큰 컨트랙트 주소
                value,
                functionData
            );
            
            // 4. JSON 형태로 반환
            String rawTransactionJson = String.format(
                "{\"fromAddress\":\"%s\",\"toAddress\":\"%s\",\"tokenAddress\":\"%s\",\"amount\":\"%s\",\"nonce\":\"%s\",\"gasPrice\":\"%s\",\"gasLimit\":\"%s\",\"value\":\"%s\",\"data\":\"%s\"}",
                fromAddress, toAddress, tokenAddress, amount, nonce, gasPrice.toString(), gasLimit.toString(), value.toString(), functionData
            );
            
            log.info("[EthereumRawTransactionBuilder] Created RawTransaction: {}", rawTransactionJson);
            return rawTransactionJson;
            
        } catch (Exception e) {
            log.error("[EthereumRawTransactionBuilder] Failed to create ERC20 RawTransaction", e);
            throw new RuntimeException("Failed to create ERC20 RawTransaction: " + e.getMessage(), e);
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
            
            // 2. 거버넌스 컨트랙트 주소 가져오기
            String governanceContractAddress = BlockchainMetadata.Ethereum.GOVERNANCE_CONTRACT_ADDRESS;
            
            // 3. 제안 생성 함수 데이터 생성 (createProposal 함수 호출)
            String functionData = createProposalCreationFunctionData(
                proposalId, title, description, proposalFee, 
                votingStartDate, votingEndDate, requiredQuorum
            );
            
            // 4. RawTransaction 생성
            BigInteger nonceBigInt = new BigInteger(nonce);
            BigInteger gasPrice = BlockchainMetadata.Ethereum.GAS_PRICE;
            BigInteger gasLimit = BlockchainMetadata.Ethereum.PROPOSAL_CREATION_GAS_LIMIT;
            BigInteger value = proposalFee.multiply(BigDecimal.valueOf(1e18)).toBigInteger(); // ETH to Wei
            
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonceBigInt,
                gasPrice,
                gasLimit,
                governanceContractAddress,
                value,
                functionData
            );
            
            // 5. JSON 형태로 반환
            String rawTransactionJson = String.format(
                "{\"fromAddress\":\"%s\",\"toAddress\":\"%s\",\"data\":\"%s\",\"value\":\"%s\",\"nonce\":\"%s\",\"gasPrice\":\"%s\",\"gasLimit\":\"%s\"}",
                walletAddress, governanceContractAddress, functionData, value.toString(), nonce, gasPrice.toString(), gasLimit.toString()
            );
            
            log.info("[EthereumRawTransactionBuilder] Created Proposal Creation RawTransaction: {}", rawTransactionJson);
            return rawTransactionJson;
            
        } catch (Exception e) {
            log.error("[EthereumRawTransactionBuilder] Failed to create Proposal Creation RawTransaction", e);
            throw new RuntimeException("Failed to create Proposal Creation RawTransaction: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String createVoteRawTransaction(
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
            String functionData = createVoteFunctionData(proposalId, voteType, reason, votingPower);
            
            // 4. RawTransaction 생성
            BigInteger nonceBigInt = new BigInteger(nonce);
            BigInteger gasPrice = BlockchainMetadata.Ethereum.GAS_PRICE;
            BigInteger gasLimit = BlockchainMetadata.Ethereum.VOTE_GAS_LIMIT;
            BigInteger value = BigInteger.ZERO; // 투표는 value가 0
            
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonceBigInt,
                gasPrice,
                gasLimit,
                governanceContractAddress,
                value,
                functionData
            );
            
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
     * ERC-20 transfer 함수 데이터 생성
     */
    private String createERC20TransferFunctionData(String toAddress, BigDecimal amount) {
        try {
            // ERC-20 transfer 함수 시그니처: transfer(address,uint256)
            BigInteger amountWei = amount.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            
            Function transferFunction = new Function(
                "transfer",
                Arrays.asList(
                    new org.web3j.abi.datatypes.Address(toAddress),
                    new Uint256(amountWei)
                ),
                Collections.emptyList()
            );
            
            return FunctionEncoder.encode(transferFunction);
        } catch (Exception e) {
            log.error("[EthereumRawTransactionBuilder] Failed to create ERC-20 transfer function data", e);
            throw new RuntimeException("Failed to create ERC-20 transfer function data: " + e.getMessage(), e);
        }
    }
    
    /**
     * 제안 생성 함수 데이터 생성
     */
    private String createProposalCreationFunctionData(
        UUID proposalId,
        String title,
        String description,
        BigDecimal proposalFee,
        LocalDateTime votingStartDate,
        LocalDateTime votingEndDate,
        BigDecimal requiredQuorum
    ) {
        try {
            // createProposal 함수 시그니처: createProposal(string title, string description, uint256 proposalFee, uint256 votingStart, uint256 votingEnd, uint256 requiredQuorum)
            long votingStartTimestamp = votingStartDate.toEpochSecond(java.time.ZoneOffset.UTC);
            long votingEndTimestamp = votingEndDate.toEpochSecond(java.time.ZoneOffset.UTC);
            
            Function createProposalFunction = new Function(
                "createProposal",
                Arrays.asList(
                    new Utf8String(title),
                    new Utf8String(description),
                    new Uint256(proposalFee.multiply(BigDecimal.valueOf(1e18)).toBigInteger()),
                    new Uint256(BigInteger.valueOf(votingStartTimestamp)),
                    new Uint256(BigInteger.valueOf(votingEndTimestamp)),
                    new Uint256(requiredQuorum.multiply(BigDecimal.valueOf(1e18)).toBigInteger())
                ),
                Arrays.asList(new TypeReference<Uint256>() {})
            );
            
            return FunctionEncoder.encode(createProposalFunction);
        } catch (Exception e) {
            log.error("[EthereumRawTransactionBuilder] Failed to create proposal creation function data", e);
            throw new RuntimeException("Failed to create proposal creation function data: " + e.getMessage(), e);
        }
    }
    
    /**
     * 투표 함수 데이터 생성
     */
    private String createVoteFunctionData(
        UUID proposalId,
        String voteType,
        String reason,
        BigDecimal votingPower
    ) {
        try {
            // vote 함수 시그니처: vote(uint256 proposalId, uint8 voteType, string reason, uint256 votingPower)
            
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
                    new Uint256(proposalId.getMostSignificantBits()),
                    new Uint8(BigInteger.valueOf(voteTypeNumber)),
                    new Utf8String(reason != null ? reason : ""),
                    new Uint256(votingPower.multiply(BigDecimal.valueOf(1e18)).toBigInteger())
                ),
                Collections.emptyList()
            );
            
            return FunctionEncoder.encode(voteFunction);
        } catch (Exception e) {
            log.error("[EthereumRawTransactionBuilder] Failed to create vote function data", e);
            throw new RuntimeException("Failed to create vote function data: " + e.getMessage(), e);
        }
    }
} 