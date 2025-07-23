package com.bloominggrace.governance.governance.infrastructure.service.ethereum;

import com.bloominggrace.governance.governance.domain.model.ProposalId;
import com.bloominggrace.governance.governance.domain.model.VoteType;
import com.bloominggrace.governance.governance.domain.service.BlockchainGovernanceService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.shared.util.HexUtils;
import com.bloominggrace.governance.shared.domain.model.TransactionBody;
import com.bloominggrace.governance.shared.domain.model.EthereumTransactionData;
import com.bloominggrace.governance.shared.domain.model.EthereumTransactionBodyFactory;
import com.bloominggrace.governance.shared.infrastructure.service.ethereum.EthereumRawTransactionBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * Ethereum 거버넌스 컨트랙트 서비스
 * Ethereum 스마트 컨트랙트 호출을 위한 트랜잭션 데이터 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EthereumGovernanceContractService implements BlockchainGovernanceService {
    
    @Value("${blockchain.ethereum.governance-contract}")
    private String governanceContractAddress;

    // Function selectors (keccak256의 첫 4바이트)
    // 실제 거버넌스 컨트랙트의 propose() 함수 시그니처
    private static final byte[] PROPOSE_SELECTOR = HexUtils.hexToBytes("0x1c4afc57");
    private static final byte[] CREATE_PROPOSAL_SELECTOR = HexUtils.hexToBytes("0x12345678");
    private static final byte[] VOTE_SELECTOR = HexUtils.hexToBytes("0x87654321");
    private static final byte[] EXECUTE_PROPOSAL_SELECTOR = HexUtils.hexToBytes("0xabcdef12");


    private static final byte[] BURN_TOKENS_SELECTOR = HexUtils.hexToBytes("0x90123456");
    private static final byte[] EXCHANGE_REQUEST_SELECTOR = HexUtils.hexToBytes("0xa1b2c3d4");
    private static final byte[] EXECUTE_EXCHANGE_SELECTOR = HexUtils.hexToBytes("0xb2c3d4e5");
    private static final byte[] COMPLETE_EXCHANGE_SELECTOR = HexUtils.hexToBytes("0xc3d4e5f6");
    
    /**
     * 설정에서 Ethereum 거버넌스 컨트랙트 주소를 가져옴
     */
    private String getGovernanceContractAddress() {
        String address = governanceContractAddress;
        if (address == null || address.equals("0x0000000000000000000000000000000000000000")) {
            log.warn("Governance contract address not configured, using default");
            return "0x0000000000000000000000000000000000000000";
        }
        return address;
    }

    @Override
    public NetworkType getSupportedNetworkType() {
        return NetworkType.ETHEREUM;
    }
    
    @Override
    public TransactionBody createProposalTransaction(
        String proposalId,
        String title,
        String description,
        String creatorWalletAddress,
        BigDecimal proposalFee,
        LocalDateTime votingStartDate,
        LocalDateTime votingEndDate,
        BigDecimal requiredQuorum) {
        
        try {
            log.info("Creating Ethereum proposal transaction - ProposalId: {}, Title: {}", proposalId, title);
            
            // 실제 거버넌스 컨트랙트의 propose() 함수 호출
            byte[] transactionData = createProposeTransactionData(title, description);
            
            // 2. EthereumTransactionData 생성
            EthereumTransactionData ethereumData = new EthereumTransactionData(
                BigInteger.valueOf(20_000_000_000L), // 20 Gwei
                BigInteger.valueOf(500000L), // 거버넌스 제안 가스 한도 증가
                BigInteger.ZERO, // 컨트랙트 호출이므로 value는 0
                getGovernanceContractAddress(),
                getGovernanceContractAddress(),
                HexUtils.bytesToHex(transactionData),
                BigInteger.ZERO
            );
            
            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                .type(TransactionBody.TransactionType.PROPOSAL_CREATE)
                .fromAddress(creatorWalletAddress)
                .toAddress(getGovernanceContractAddress())
                .data(HexUtils.bytesToHex(transactionData))
                .networkType(NetworkType.ETHEREUM.name())
                .networkSpecificData(ethereumData)
                .build();
            
        } catch (Exception e) {
            log.error("Error creating Ethereum proposal transaction", e);
            throw new RuntimeException("Failed to create Ethereum proposal transaction", e);
        }
    }
    
    /**
     * 실제 거버넌스 컨트랙트의 propose() 함수 호출을 위한 트랜잭션 데이터 생성
     * propose(string memory description, string memory details)
     */
    public byte[] createProposeTransactionData(String description, String details) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            // 1. Function selector (propose 함수)
            buffer.put(PROPOSE_SELECTOR);
            
            // 2. Description (string) - 동적 타입이므로 오프셋 위치
            buffer.put(new byte[32]); // description의 오프셋 (나중에 계산)
            
            // 3. Details (string) - 동적 타입이므로 오프셋 위치  
            buffer.put(new byte[32]); // details의 오프셋 (나중에 계산)
            
            // 4. Description 문자열 데이터
            byte[] descriptionBytes = description.getBytes(StandardCharsets.UTF_8);
            int descriptionLength = descriptionBytes.length;
            buffer.put(BigInteger.valueOf(descriptionLength).toByteArray());
            buffer.put(descriptionBytes);
            
            // 5. Details 문자열 데이터
            byte[] detailsBytes = details.getBytes(StandardCharsets.UTF_8);
            int detailsLength = detailsBytes.length;
            buffer.put(BigInteger.valueOf(detailsLength).toByteArray());
            buffer.put(detailsBytes);
            
            // 6. 오프셋 값들을 올바른 위치에 설정
            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            
            // Description 오프셋: 64 (두 개의 uint256 오프셋 이후)
            byte[] descriptionOffset = BigInteger.valueOf(64).toByteArray();
            System.arraycopy(descriptionOffset, 0, result, 4, descriptionOffset.length);
            
            // Details 오프셋: 64 + description 길이 + 32 (description 길이 필드)
            int detailsOffset = 64 + 32 + descriptionLength;
            byte[] detailsOffsetBytes = BigInteger.valueOf(detailsOffset).toByteArray();
            System.arraycopy(detailsOffsetBytes, 0, result, 36, detailsOffsetBytes.length);
            
            log.info("Created Ethereum propose transaction data: {} bytes", result.length);
            log.info("Description: '{}', Details: '{}'", description, details);
            return result;
            
        } catch (Exception e) {
            log.error("Error creating Ethereum propose transaction data", e);
            throw new RuntimeException("Failed to create Ethereum propose transaction data", e);
        }
    }
    
    @Override
    public TransactionBody createVoteTransaction(
        String proposalId,
        String voterWalletAddress,
        VoteType voteType,
        BigDecimal votingPower,
        String reason) {
        
        try {
            log.info("Creating Ethereum vote transaction - ProposalId: {}, Voter: {}, VoteType: {}", 
                proposalId, voterWalletAddress, voteType);
            
            // 1. 트랜잭션 데이터 생성
            byte[] transactionData = createVoteTransactionData(
                new ProposalId(UUID.fromString(proposalId)),
                voterWalletAddress,
                voteType,
                votingPower,
                reason
            );
            
            // 2. EthereumTransactionData 생성
            EthereumTransactionData ethereumData = new EthereumTransactionData(
                BigInteger.valueOf(20_000_000_000L), // 20 Gwei
                BigInteger.valueOf(21000L), // 기본 가스 한도
                BigInteger.ZERO, // 컨트랙트 호출이므로 value는 0
                getGovernanceContractAddress(),
                getGovernanceContractAddress(),
                HexUtils.bytesToHex(transactionData),
                BigInteger.ZERO
            );
            
            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                .type(TransactionBody.TransactionType.PROPOSAL_VOTE)
                .fromAddress(voterWalletAddress)
                .toAddress(getGovernanceContractAddress())
                .data(HexUtils.bytesToHex(transactionData))
                .networkType(NetworkType.ETHEREUM.name())
                .networkSpecificData(ethereumData)
                .build();
            
        } catch (Exception e) {
            log.error("Error creating Ethereum vote transaction", e);
            throw new RuntimeException("Failed to create Ethereum vote transaction", e);
        }
    }
    
    @Override
    public byte[] createExecuteProposalTransactionData(
        ProposalId proposalId,
        String executorWalletAddress) {
        
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            // 1. Function selector
            buffer.put(EXECUTE_PROPOSAL_SELECTOR);
            
            // 2. Proposal ID (bytes32)
            byte[] proposalIdBytes = proposalId.getValue().toString().getBytes(StandardCharsets.UTF_8);
            buffer.put(proposalIdBytes);
            buffer.put(new byte[32 - proposalIdBytes.length]);
            
            // 3. Executor address (address)
            byte[] executorAddress = HexUtils.hexToBytes(executorWalletAddress.substring(2));
            buffer.put(new byte[12]);
            buffer.put(executorAddress);
            
            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created Ethereum execute proposal transaction data: {} bytes", result.length);
            return result;
            
        } catch (Exception e) {
            log.error("Error creating Ethereum execute proposal transaction data", e);
            throw new RuntimeException("Failed to create Ethereum execute proposal transaction data", e);
        }
    }
    
    
    

    
    @Override
    public byte[] createBurnGovernanceTokensTransactionData(
        String burnerWalletAddress,
        BigDecimal amount) {
        
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            // 1. Function selector
            buffer.put(BURN_TOKENS_SELECTOR);
            
            // 2. Burner address (address)
            byte[] burnerAddress = HexUtils.hexToBytes(burnerWalletAddress.substring(2));
            buffer.put(new byte[12]);
            buffer.put(burnerAddress);
            
            // 3. Amount (uint256)
            BigInteger amountWei = amount.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            byte[] amountBytes = amountWei.toByteArray();
            buffer.put(new byte[32 - amountBytes.length]);
            buffer.put(amountBytes);
            
            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created Ethereum burn governance tokens transaction data: {} bytes", result.length);
            return result;
            
        } catch (Exception e) {
            log.error("Error creating Ethereum burn governance tokens transaction data", e);
            throw new RuntimeException("Failed to create Ethereum burn governance tokens transaction data", e);
        }
    }
    
    @Override
    public ProposalStatusInfo getProposalStatusFromBlockchain(ProposalId proposalId) {
        // 실제로는 Ethereum RPC를 통해 제안 상태를 조회
        return new ProposalStatusInfo(
            proposalId,
            "ACTIVE",
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(7),
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(500)
        );
    }
    
    @Override
    public VoteResultInfo getVoteResultsFromBlockchain(ProposalId proposalId) {
        // 실제로는 Ethereum RPC를 통해 투표 결과를 조회
        return new VoteResultInfo(
            proposalId,
            BigDecimal.valueOf(300),
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(500),
            "ACTIVE"
        );
    }
    

    
    @Override
    public boolean verifyTransactionSignature(
        byte[] transactionData,
        byte[] signature,
        String publicKey) {
        
        try {
            // 실제로는 Ethereum 서명 검증 로직 구현
            log.info("Verifying Ethereum transaction signature for public key: {}", publicKey);
            return true; // 임시로 true 반환
        } catch (Exception e) {
            log.error("Error verifying Ethereum transaction signature", e);
            return false;
        }
    }
    
    @Override
    public byte[] createProposalTransactionData(
        ProposalId proposalId,
        String title,
        String description,
        String creatorWalletAddress,
        BigDecimal proposalFee) {
        
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            // 1. Function selector
            buffer.put(CREATE_PROPOSAL_SELECTOR);
            
            // 2. Proposal ID (bytes32)
            byte[] proposalIdBytes = proposalId.getValue().toString().getBytes(StandardCharsets.UTF_8);
            buffer.put(proposalIdBytes);
            buffer.put(new byte[32 - proposalIdBytes.length]);
            
            // 3. Title (string)
            byte[] titleBytes = title.getBytes(StandardCharsets.UTF_8);
            buffer.put(titleBytes);
            buffer.put(new byte[32 - titleBytes.length]);
            
            // 4. Description (string)
            byte[] descriptionBytes = description.getBytes(StandardCharsets.UTF_8);
            buffer.put(descriptionBytes);
            buffer.put(new byte[32 - descriptionBytes.length]);
            
            // 5. Creator address (address)
            byte[] creatorAddress = HexUtils.hexToBytes(creatorWalletAddress.substring(2));
            buffer.put(new byte[12]);
            buffer.put(creatorAddress);
            
            // 6. Proposal fee (uint256)
            BigInteger feeWei = proposalFee.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            byte[] feeBytes = feeWei.toByteArray();
            buffer.put(new byte[32 - feeBytes.length]);
            buffer.put(feeBytes);
            
            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created Ethereum proposal transaction data: {} bytes", result.length);
            return result;
            
        } catch (Exception e) {
            log.error("Error creating Ethereum proposal transaction data", e);
            throw new RuntimeException("Failed to create Ethereum proposal transaction data", e);
        }
    }
    
    @Override
    public byte[] createVoteTransactionData(
        ProposalId proposalId,
        String voterWalletAddress,
        VoteType voteType,
        BigDecimal votingPower,
        String reason) {
        
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            // 1. Function selector
            buffer.put(VOTE_SELECTOR);
            
            // 2. Proposal ID (bytes32)
            byte[] proposalIdBytes = proposalId.getValue().toString().getBytes(StandardCharsets.UTF_8);
            buffer.put(proposalIdBytes);
            buffer.put(new byte[32 - proposalIdBytes.length]);
            
            // 3. Voter address (address)
            byte[] voterAddress = HexUtils.hexToBytes(voterWalletAddress.substring(2));
            buffer.put(new byte[12]);
            buffer.put(voterAddress);
            
            // 4. Vote type (uint8)
            buffer.put((byte) voteType.ordinal());
            buffer.put(new byte[31]); // 패딩
            
            // 5. Voting power (uint256)
            BigInteger votingPowerWei = votingPower.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            byte[] votingPowerBytes = votingPowerWei.toByteArray();
            buffer.put(new byte[32 - votingPowerBytes.length]);
            buffer.put(votingPowerBytes);
            
            // 6. Reason (string)
            byte[] reasonBytes = reason.getBytes(StandardCharsets.UTF_8);
            buffer.put(reasonBytes);
            buffer.put(new byte[32 - reasonBytes.length]);
            
            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created Ethereum vote transaction data: {} bytes", result.length);
            return result;
            
        } catch (Exception e) {
            log.error("Error creating Ethereum vote transaction data", e);
            throw new RuntimeException("Failed to create Ethereum vote transaction data", e);
        }
    }
    
    @Override
    public TransactionBody createExchangePointsToTokensTransaction(
        String userWalletAddress,
        BigDecimal pointAmount,
        BigDecimal tokenAmount,
        String description) {
        
        try {
            log.info("Creating Ethereum exchange points to tokens transaction - User: {}, Points: {}, Tokens: {}", 
                userWalletAddress, pointAmount, tokenAmount);
            
            // 1. 트랜잭션 데이터 생성
            byte[] transactionData = createExchangeRequestTransactionData(
                userWalletAddress,
                pointAmount,
                tokenAmount,
                "POINTS_TO_TOKENS"
            );
            
            // 2. EthereumTransactionData 생성
            EthereumTransactionData ethereumData = new EthereumTransactionData(
                BigInteger.valueOf(20_000_000_000L), // 20 Gwei
                BigInteger.valueOf(21000L), // 기본 가스 한도
                BigInteger.ZERO, // 컨트랙트 호출이므로 value는 0
                getGovernanceContractAddress(),
                getGovernanceContractAddress(),
                HexUtils.bytesToHex(transactionData),
                BigInteger.ZERO
            );
            
            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                .type(TransactionBody.TransactionType.TOKEN_TRANSFER)
                .fromAddress(userWalletAddress)
                .toAddress(getGovernanceContractAddress())
                .data(HexUtils.bytesToHex(transactionData))
                .networkType(NetworkType.ETHEREUM.name())
                .networkSpecificData(ethereumData)
                .build();
            
        } catch (Exception e) {
            log.error("Error creating Ethereum exchange points to tokens transaction", e);
            throw new RuntimeException("Failed to create Ethereum exchange points to tokens transaction", e);
        }
    }
    
    @Override
    public TransactionBody createExchangeTokensToPointsTransaction(
        String userWalletAddress,
        BigDecimal tokenAmount,
        BigDecimal pointAmount,
        String description) {
        
        try {
            log.info("Creating Ethereum exchange tokens to points transaction - User: {}, Tokens: {}, Points: {}", 
                userWalletAddress, tokenAmount, pointAmount);
            
            // 1. 트랜잭션 데이터 생성
            byte[] transactionData = createExchangeRequestTransactionData(
                userWalletAddress,
                pointAmount,
                tokenAmount,
                "TOKENS_TO_POINTS"
            );
            
            // 2. EthereumTransactionData 생성
            EthereumTransactionData ethereumData = new EthereumTransactionData(
                BigInteger.valueOf(20_000_000_000L), // 20 Gwei
                BigInteger.valueOf(21000L), // 기본 가스 한도
                BigInteger.ZERO, // 컨트랙트 호출이므로 value는 0
                getGovernanceContractAddress(),
                getGovernanceContractAddress(),
                HexUtils.bytesToHex(transactionData),
                BigInteger.ZERO
            );
            
            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                .type(TransactionBody.TransactionType.TOKEN_TRANSFER)
                .fromAddress(userWalletAddress)
                .toAddress(getGovernanceContractAddress())
                .data(HexUtils.bytesToHex(transactionData))
                .networkType(NetworkType.ETHEREUM.name())
                .networkSpecificData(ethereumData)
                .build();
            
        } catch (Exception e) {
            log.error("Error creating Ethereum exchange tokens to points transaction", e);
            throw new RuntimeException("Failed to create Ethereum exchange tokens to points transaction", e);
        }
    }
    
    @Override
    public byte[] createExchangeRequestTransactionData(
        String userWalletAddress,
        BigDecimal pointAmount,
        BigDecimal tokenAmount,
        String exchangeType) {
        
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            // 1. Function selector
            buffer.put(EXCHANGE_REQUEST_SELECTOR);
            
            // 2. User address (address)
            byte[] userAddress = HexUtils.hexToBytes(userWalletAddress.substring(2));
            buffer.put(new byte[12]);
            buffer.put(userAddress);
            
            // 3. Point amount (uint256)
            BigInteger pointAmountWei = pointAmount.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            byte[] pointAmountBytes = pointAmountWei.toByteArray();
            buffer.put(new byte[32 - pointAmountBytes.length]);
            buffer.put(pointAmountBytes);
            
            // 4. Token amount (uint256)
            BigInteger tokenAmountWei = tokenAmount.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            byte[] tokenAmountBytes = tokenAmountWei.toByteArray();
            buffer.put(new byte[32 - tokenAmountBytes.length]);
            buffer.put(tokenAmountBytes);
            
            // 5. Exchange type (string)
            byte[] exchangeTypeBytes = exchangeType.getBytes(StandardCharsets.UTF_8);
            buffer.put(exchangeTypeBytes);
            buffer.put(new byte[32 - exchangeTypeBytes.length]);
            
            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created Ethereum exchange request transaction data: {} bytes", result.length);
            return result;
            
        } catch (Exception e) {
            log.error("Error creating Ethereum exchange request transaction data", e);
            throw new RuntimeException("Failed to create Ethereum exchange request transaction data", e);
        }
    }
    
    @Override
    public byte[] createExecuteExchangeTransactionData(
        String userWalletAddress,
        BigDecimal pointAmount,
        BigDecimal tokenAmount,
        String exchangeType) {
        
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            // 1. Function selector
            buffer.put(EXECUTE_EXCHANGE_SELECTOR);
            
            // 2. User address (address)
            byte[] userAddress = HexUtils.hexToBytes(userWalletAddress.substring(2));
            buffer.put(new byte[12]);
            buffer.put(userAddress);
            
            // 3. Point amount (uint256)
            BigInteger pointAmountWei = pointAmount.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            byte[] pointAmountBytes = pointAmountWei.toByteArray();
            buffer.put(new byte[32 - pointAmountBytes.length]);
            buffer.put(pointAmountBytes);
            
            // 4. Token amount (uint256)
            BigInteger tokenAmountWei = tokenAmount.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            byte[] tokenAmountBytes = tokenAmountWei.toByteArray();
            buffer.put(new byte[32 - tokenAmountBytes.length]);
            buffer.put(tokenAmountBytes);
            
            // 5. Exchange type (string)
            byte[] exchangeTypeBytes = exchangeType.getBytes(StandardCharsets.UTF_8);
            buffer.put(exchangeTypeBytes);
            buffer.put(new byte[32 - exchangeTypeBytes.length]);
            
            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created Ethereum execute exchange transaction data: {} bytes", result.length);
            return result;
            
        } catch (Exception e) {
            log.error("Error creating Ethereum execute exchange transaction data", e);
            throw new RuntimeException("Failed to create Ethereum execute exchange transaction data", e);
        }
    }
    
    @Override
    public ExchangeInfo getExchangeInfoFromBlockchain(String walletAddress) {
        // 실제 구현에서는 블록체인에서 교환 정보를 조회
        log.info("Getting exchange info from Ethereum blockchain for wallet: {}", walletAddress);
        
        return new ExchangeInfo(
            walletAddress,
            BigDecimal.valueOf(100), // 기본 교환 비율
            BigDecimal.valueOf(10),  // 최소 교환 금액
            BigDecimal.valueOf(3600), // 쿨다운 기간 (초)
            LocalDateTime.now().minusHours(1) // 마지막 교환 시간
        );
    }

    @Override
    public TransactionBody createCompleteExchangeTransaction(
            String adminWalletAddress,
            String userWalletAddress,
            BigDecimal tokenAmount,
            String description) {

        try {
            log.info("Creating Ethereum complete exchange transaction - Admin: {}, User: {}, TokenAmount: {}",
                    adminWalletAddress, userWalletAddress, tokenAmount);

            // 1. 트랜잭션 데이터 생성
            byte[] transactionData = createCompleteExchangeTransactionData(
                    adminWalletAddress,
                    userWalletAddress,
                    tokenAmount
            );

            // 2. EthereumTransactionData 생성
            EthereumTransactionData ethereumData = new EthereumTransactionData(
                    BigInteger.valueOf(20_000_000_000L), // 20 Gwei
                    BigInteger.valueOf(21000L), // 기본 가스 한도
                    BigInteger.ZERO, // 컨트랙트 호출이므로 value는 0
                    getGovernanceContractAddress(),
                    getGovernanceContractAddress(),
                    HexUtils.bytesToHex(transactionData),
                    BigInteger.ZERO
            );

            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                    .type(TransactionBody.TransactionType.TOKEN_TRANSFER)
                    .fromAddress(adminWalletAddress)
                    .toAddress(userWalletAddress)
                    .data(HexUtils.bytesToHex(transactionData))
                    .networkType(NetworkType.ETHEREUM.name())
                    .networkSpecificData(ethereumData)
                    .build();

        } catch (Exception e) {
            log.error("Error creating Ethereum complete exchange transaction", e);
            throw new RuntimeException("Failed to create Ethereum complete exchange transaction", e);
        }
    }

    @Override
    public byte[] createCompleteExchangeTransactionData(
            String adminWalletAddress,
            String userWalletAddress,
            BigDecimal tokenAmount) {
        
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            // 1. Function selector
            buffer.put(COMPLETE_EXCHANGE_SELECTOR);
            
            // 2. Admin wallet address (address)
            byte[] adminAddress = HexUtils.hexToBytes(adminWalletAddress.substring(2));
            buffer.put(new byte[12]);
            buffer.put(adminAddress);
            
            // 3. User wallet address (address)
            byte[] userAddress = HexUtils.hexToBytes(userWalletAddress.substring(2));
            buffer.put(new byte[12]);
            buffer.put(userAddress);
            
            // 4. Token amount (uint256)
            byte[] amountBytes = tokenAmount.toBigInteger().toByteArray();
            buffer.put(new byte[32 - amountBytes.length]);
            buffer.put(amountBytes);
            
            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created complete exchange transaction data: {} bytes", result.length);
            return result;
            
        } catch (Exception e) {
            log.error("Error creating complete exchange transaction data", e);
            throw new RuntimeException("Failed to create complete exchange transaction data", e);
        }
    }
    
    // ===== 토큰 관련 트랜잭션 =====
    
    @Override
    public TransactionBody createTokenTransferTransaction(
            String fromAddress,
            String toAddress,
            BigDecimal amount,
            String tokenContract) {

        try {
            log.info("Creating Ethereum token transfer transaction - From: {}, To: {}, Amount: {}", 
                fromAddress, toAddress, amount);

            // 1. 트랜잭션 데이터 생성
            byte[] transactionData = createTokenTransferTransactionData(fromAddress, toAddress, amount, tokenContract);

            // 2. EthereumTransactionData 생성
            EthereumTransactionData ethereumData = new EthereumTransactionData(
                BigInteger.valueOf(20_000_000_000L), // 20 Gwei
                BigInteger.valueOf(65000L), // ERC20 전송 가스 한도
                BigInteger.ZERO, // 토큰 전송이므로 value는 0
                tokenContract,
                tokenContract,
                HexUtils.bytesToHex(transactionData),
                BigInteger.ZERO
            );

            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                .type(TransactionBody.TransactionType.TOKEN_TRANSFER)
                .fromAddress(fromAddress)
                .toAddress(tokenContract)
                .data(HexUtils.bytesToHex(transactionData))
                .networkType(NetworkType.ETHEREUM.name())
                .networkSpecificData(ethereumData)
                .build();

        } catch (Exception e) {
            log.error("Error creating Ethereum token transfer transaction", e);
            throw new RuntimeException("Failed to create Ethereum token transfer transaction", e);
        }
    }

    @Override
    public TransactionBody createTokenMintTransaction(
            String toAddress,
            BigDecimal amount,
            String tokenContract) {

        try {
            log.info("Creating Ethereum token mint transaction - To: {}, Amount: {}", toAddress, amount);

            // 1. 트랜잭션 데이터 생성
            byte[] transactionData = createTokenMintTransactionData(toAddress, amount, tokenContract);

            // 2. EthereumTransactionData 생성
            EthereumTransactionData ethereumData = new EthereumTransactionData(
                BigInteger.valueOf(20_000_000_000L), // 20 Gwei
                BigInteger.valueOf(500000L), // 민팅 가스 한도 대폭 증가
                BigInteger.ZERO, // 민팅이므로 value는 0
                tokenContract,
                tokenContract,
                HexUtils.bytesToHex(transactionData),
                BigInteger.ZERO
            );

            // 3. TransactionBody 생성 및 반환
            // Admin 지갑에서 민팅하므로 fromAddress는 Admin 지갑 주소로 설정
            String adminWalletAddress = "0x55D5c49e36f8A89111687C9DC8355121068f0cD8";
            
            return TransactionBody.builder()
                .type(TransactionBody.TransactionType.TOKEN_MINT)
                .fromAddress(adminWalletAddress) // Admin 지갑에서 민팅
                .toAddress(tokenContract)
                .data(HexUtils.bytesToHex(transactionData))
                .networkType(NetworkType.ETHEREUM.name())
                .networkSpecificData(ethereumData)
                .build();

        } catch (Exception e) {
            log.error("Error creating Ethereum token mint transaction", e);
            throw new RuntimeException("Failed to create Ethereum token mint transaction", e);
        }
    }

    @Override
    public TransactionBody createTokenBurnTransaction(
            String fromAddress,
            BigDecimal amount,
            String tokenContract) {

        try {
            log.info("Creating Ethereum token burn transaction - From: {}, Amount: {}", fromAddress, amount);

            // 1. 트랜잭션 데이터 생성
            byte[] transactionData = createTokenBurnTransactionData(fromAddress, amount, tokenContract);

            // 2. EthereumTransactionData 생성
            EthereumTransactionData ethereumData = new EthereumTransactionData(
                BigInteger.valueOf(20_000_000_000L), // 20 Gwei
                BigInteger.valueOf(50000L), // 소각 가스 한도
                BigInteger.ZERO, // 소각이므로 value는 0
                tokenContract,
                tokenContract,
                HexUtils.bytesToHex(transactionData),
                BigInteger.ZERO
            );

            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                .type(TransactionBody.TransactionType.TOKEN_BURN)
                .fromAddress(fromAddress)
                .toAddress(tokenContract)
                .data(HexUtils.bytesToHex(transactionData))
                .networkType(NetworkType.ETHEREUM.name())
                .networkSpecificData(ethereumData)
                .build();

        } catch (Exception e) {
            log.error("Error creating Ethereum token burn transaction", e);
            throw new RuntimeException("Failed to create Ethereum token burn transaction", e);
        }
    }

    @Override
    public TransactionBody createExchangeTransaction(
            String userAddress,
            BigDecimal amount,
            String exchangeType,
            String tokenContract) {

        try {
            log.info("Creating Ethereum exchange transaction - User: {}, Amount: {}, Type: {}", 
                userAddress, amount, exchangeType);

            // 1. 트랜잭션 데이터 생성
            byte[] transactionData = createExchangeTransactionData(userAddress, amount, exchangeType, tokenContract);

            // 2. EthereumTransactionData 생성
            EthereumTransactionData ethereumData = new EthereumTransactionData(
                BigInteger.valueOf(20_000_000_000L), // 20 Gwei
                BigInteger.valueOf(100000L), // 교환 가스 한도
                BigInteger.ZERO, // 교환이므로 value는 0
                getGovernanceContractAddress(),
                getGovernanceContractAddress(),
                HexUtils.bytesToHex(transactionData),
                BigInteger.ZERO
            );

            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                .type(TransactionBody.TransactionType.TOKEN_TRANSFER)
                .fromAddress(userAddress)
                .toAddress(getGovernanceContractAddress())
                .data(HexUtils.bytesToHex(transactionData))
                .networkType(NetworkType.ETHEREUM.name())
                .networkSpecificData(ethereumData)
                .build();

        } catch (Exception e) {
            log.error("Error creating Ethereum exchange transaction", e);
            throw new RuntimeException("Failed to create Ethereum exchange transaction", e);
        }
    }
    
    // ===== 토큰 관련 트랜잭션 데이터 생성 메서드들 =====
    
    private byte[] createTokenTransferTransactionData(String fromAddress, String toAddress, BigDecimal amount, String tokenContract) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            // ERC20 transfer function selector (transfer(address,uint256))
            buffer.put(HexUtils.hexToBytes("0xa9059cbb"));
            
            // To address (address)
            byte[] toAddr = HexUtils.hexToBytes(toAddress.substring(2));
            buffer.put(new byte[12]);
            buffer.put(toAddr);
            
            // Amount (uint256)
            byte[] amountBytes = amount.toBigInteger().toByteArray();
            buffer.put(new byte[32 - amountBytes.length]);
            buffer.put(amountBytes);
            
            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created token transfer transaction data: {} bytes", result.length);
            return result;
            
        } catch (Exception e) {
            log.error("Error creating token transfer transaction data", e);
            throw new RuntimeException("Failed to create token transfer transaction data", e);
        }
    }
    
    private byte[] createTokenMintTransactionData(String toAddress, BigDecimal amount, String tokenContract) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            // ERC20 mint function selector (mint(address,uint256))
            // 실제 OpenZeppelin ERC20의 _mint 함수 시그니처
            buffer.put(HexUtils.hexToBytes("0x40c10f19"));
            
            // To address (address)
            byte[] toAddr = HexUtils.hexToBytes(toAddress.substring(2));
            buffer.put(new byte[12]);
            buffer.put(toAddr);
            
            // Amount (uint256) - Wei 단위로 변환 (18자리 소수점)
            BigInteger amountWei = amount.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            byte[] amountBytes = amountWei.toByteArray();
            buffer.put(new byte[32 - amountBytes.length]);
            buffer.put(amountBytes);
            
            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created token mint transaction data: {} bytes", result.length);
            return result;
            
        } catch (Exception e) {
            log.error("Error creating token mint transaction data", e);
            throw new RuntimeException("Failed to create token mint transaction data", e);
        }
    }
    
    private byte[] createTokenBurnTransactionData(String fromAddress, BigDecimal amount, String tokenContract) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            // ERC20 burn function selector (burn(uint256))
            buffer.put(HexUtils.hexToBytes("0x42966c68"));
            
            // Amount (uint256)
            byte[] amountBytes = amount.toBigInteger().toByteArray();
            buffer.put(new byte[32 - amountBytes.length]);
            buffer.put(amountBytes);
            
            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created token burn transaction data: {} bytes", result.length);
            return result;
            
        } catch (Exception e) {
            log.error("Error creating token burn transaction data", e);
            throw new RuntimeException("Failed to create token burn transaction data", e);
        }
    }
    
    private byte[] createExchangeTransactionData(String userAddress, BigDecimal amount, String exchangeType, String tokenContract) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            // Exchange function selector
            buffer.put(EXCHANGE_REQUEST_SELECTOR);
            
            // User address (address)
            byte[] userAddr = HexUtils.hexToBytes(userAddress.substring(2));
            buffer.put(new byte[12]);
            buffer.put(userAddr);
            
            // Amount (uint256)
            byte[] amountBytes = amount.toBigInteger().toByteArray();
            buffer.put(new byte[32 - amountBytes.length]);
            buffer.put(amountBytes);
            
            // Exchange type (string)
            byte[] exchangeTypeBytes = exchangeType.getBytes(StandardCharsets.UTF_8);
            buffer.put(exchangeTypeBytes);
            
            // Token contract (address)
            byte[] tokenContractBytes = HexUtils.hexToBytes(tokenContract.substring(2));
            buffer.put(new byte[12]);
            buffer.put(tokenContractBytes);
            
            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created exchange transaction data: {} bytes", result.length);
            return result;
            
        } catch (Exception e) {
            log.error("Error creating exchange transaction data", e);
            throw new RuntimeException("Failed to create exchange transaction data", e);
        }
    }
} 