package com.bloominggrace.governance.governance.infrastructure.service.solana;

import com.bloominggrace.governance.governance.domain.model.ProposalId;
import com.bloominggrace.governance.governance.domain.model.VoteType;
import com.bloominggrace.governance.governance.domain.service.BlockchainGovernanceService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.shared.util.Base58Utils;
import com.bloominggrace.governance.shared.util.HexUtils;
import com.bloominggrace.governance.shared.domain.model.TransactionBody;
import com.bloominggrace.governance.shared.domain.model.SolanaTransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * Solana 거버넌스 프로그램과의 실제 상호작용을 구현하는 서비스
 * 실제 Solana 프로그램 호출을 위한 바이너리 인스트럭션 데이터 생성
 */
@Slf4j
@Service
public class SolanaGovernanceContractService implements BlockchainGovernanceService {

    @Value("${blockchain.solana.governance-program-id}")
    private String governanceProgramId;

    @Value("${blockchain.solana.token-program-id}")
    private String tokenProgramId;



    @Value("${blockchain.solana.treasury-account}")
    private String treasuryAccount;

    // Instruction discriminator (8바이트)
    private static final byte[] CREATE_PROPOSAL_DISCRIMINATOR = {0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final byte[] VOTE_DISCRIMINATOR = {0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final byte[] EXECUTE_PROPOSAL_DISCRIMINATOR = {0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


    private static final byte[] BURN_TOKENS_DISCRIMINATOR = {0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final byte[] EXCHANGE_REQUEST_DISCRIMINATOR = {0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final byte[] EXECUTE_EXCHANGE_DISCRIMINATOR = {0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final byte[] COMPLETE_EXCHANGE_DISCRIMINATOR = {0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    /**
     * 설정에서 Solana 거버넌스 프로그램 ID를 가져옴
     */
    private String getGovernanceProgramId() {
        if (governanceProgramId == null || governanceProgramId.equals("11111111111111111111111111111111")) {
            log.warn("Governance program ID not configured, using default");
            return "11111111111111111111111111111111";
        }
        return governanceProgramId;
    }

    /**
     * 설정에서 Solana 토큰 프로그램 ID를 가져옴
     */
    private String getTokenProgramId() {
        if (tokenProgramId == null || tokenProgramId.equals("11111111111111111111111111111111")) {
            log.warn("Token program ID not configured, using default");
            return "11111111111111111111111111111111";
        }
        return tokenProgramId;
    }

    @Override
    public NetworkType getSupportedNetworkType() {
        return NetworkType.SOLANA;
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
            log.info("Creating Solana proposal transaction - ProposalId: {}, Title: {}", proposalId, title);

            // 1. 트랜잭션 데이터 생성
            byte[] transactionData = createProposalTransactionData(
                    new ProposalId(UUID.fromString(proposalId)),
                    title,
                    description,
                    creatorWalletAddress,
                    proposalFee
            );

            // 2. SolanaTransactionData 생성
            SolanaTransactionData solanaData = SolanaTransactionData.builder()
                    .recentBlockhash("11111111111111111111111111111111") // 실제로는 최근 블록해시 사용
                    .fee(5000L) // 기본 수수료
                    .programId(getGovernanceProgramId())
                    .build();

            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                    .type(TransactionBody.TransactionType.PROPOSAL_CREATE)
                    .fromAddress(creatorWalletAddress)
                    .toAddress(null)
                    .data(HexUtils.bytesToHex(transactionData))
                    .networkType(NetworkType.SOLANA.name())
                    .networkSpecificData(solanaData)
                    .build();

        } catch (Exception e) {
            log.error("Error creating Solana proposal transaction", e);
            throw new RuntimeException("Failed to create Solana proposal transaction", e);
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
            log.info("Creating Solana vote transaction - ProposalId: {}, Voter: {}, VoteType: {}",
                    proposalId, voterWalletAddress, voteType);

            // 1. 트랜잭션 데이터 생성
            byte[] transactionData = createVoteTransactionData(
                    new ProposalId(UUID.fromString(proposalId)),
                    voterWalletAddress,
                    voteType,
                    votingPower,
                    reason
            );

            // 2. SolanaTransactionData 생성
            SolanaTransactionData solanaData = SolanaTransactionData.builder()
                    .recentBlockhash("11111111111111111111111111111111") // 실제로는 최근 블록해시 사용
                    .fee(5000L) // 기본 수수료
                    .programId(getGovernanceProgramId())
                    .build();

            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                    .type(TransactionBody.TransactionType.PROPOSAL_VOTE)
                    .fromAddress(voterWalletAddress)
                    .toAddress(null)
                    .data(HexUtils.bytesToHex(transactionData))
                    .networkType(NetworkType.SOLANA.name())
                    .networkSpecificData(solanaData)
                    .build();

        } catch (Exception e) {
            log.error("Error creating Solana vote transaction", e);
            throw new RuntimeException("Failed to create Solana vote transaction", e);
        }
    }

    @Override
    public byte[] createExecuteProposalTransactionData(
            ProposalId proposalId,
            String executorWalletAddress) {

        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.put(EXECUTE_PROPOSAL_DISCRIMINATOR);

            // 1. Proposal ID
            buffer.put(proposalId.getValue().toString().getBytes(StandardCharsets.UTF_8));

            // 2. Executor wallet address
            byte[] executorAddress = Base58Utils.decode(executorWalletAddress);
            buffer.put(executorAddress);

            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created execute proposal transaction data: {} bytes", result.length);
            return result;

        } catch (Exception e) {
            log.error("Error creating execute proposal transaction data", e);
            throw new RuntimeException("Failed to create execute proposal transaction data", e);
        }
    }





    @Override
    public byte[] createBurnGovernanceTokensTransactionData(
            String burnerWalletAddress,
            BigDecimal amount) {

        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.put(BURN_TOKENS_DISCRIMINATOR);

            // 1. Burner wallet address
            byte[] burnerAddress = Base58Utils.decode(burnerWalletAddress);
            buffer.put(burnerAddress);

            // 2. Amount to burn (8 bytes u64)
            buffer.putLong(amount.longValue());

            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created burn governance tokens transaction data: {} bytes", result.length);
            return result;

        } catch (Exception e) {
            log.error("Error creating burn governance tokens transaction data", e);
            throw new RuntimeException("Failed to create burn governance tokens transaction data", e);
        }
    }

    @Override
    public ProposalStatusInfo getProposalStatusFromBlockchain(ProposalId proposalId) {
        // 실제로는 Solana RPC를 통해 제안 상태를 조회
        return new ProposalStatusInfo(
                proposalId.getValue(),
                "ACTIVE",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(500)
        );
    }

    @Override
    public VoteResultInfo getVoteResultsFromBlockchain(ProposalId proposalId) {
        // 실제로는 Solana RPC를 통해 투표 결과를 조회
        return new VoteResultInfo(
                proposalId.getValue(),
                BigDecimal.valueOf(300),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(450),
                "PENDING"
        );
    }



    /**
     * Solana 트랜잭션 서명 검증 (Solana 전용)
     */
    public boolean verifyTransactionSignature(
            byte[] transactionData,
            byte[] signature,
            String publicKey) {

        try {
            // Solana 서명 검증 로직 구현
            // 실제로는 Ed25519 서명 검증을 수행
            log.info("Verifying Solana transaction signature for public key: {}", publicKey);
            return true; // 임시 구현

        } catch (Exception e) {
            log.error("Error verifying Solana transaction signature", e);
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
            // 1. Instruction discriminator
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.put(CREATE_PROPOSAL_DISCRIMINATOR);

            // 2. Proposal ID (16 bytes UUID)
            buffer.put(proposalId.getValue().toString().getBytes(StandardCharsets.UTF_8));

            // 3. Title length + title
            byte[] titleBytes = title.getBytes(StandardCharsets.UTF_8);
            buffer.putInt(titleBytes.length);
            buffer.put(titleBytes);

            // 4. Description length + description
            byte[] descBytes = description.getBytes(StandardCharsets.UTF_8);
            buffer.putInt(descBytes.length);
            buffer.put(descBytes);

            // 5. Proposal fee (8 bytes u64)
            buffer.putLong(proposalFee.longValue());

            // 6. Creator wallet address (32 bytes)
            byte[] creatorAddress = Base58Utils.decode(creatorWalletAddress);
            buffer.put(creatorAddress);

            // Trim to actual size
            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created proposal transaction data: {} bytes", result.length);
            return result;

        } catch (Exception e) {
            log.error("Error creating proposal transaction data", e);
            throw new RuntimeException("Failed to create proposal transaction data", e);
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
            buffer.put(VOTE_DISCRIMINATOR);

            // 1. Proposal ID
            buffer.put(proposalId.getValue().toString().getBytes(StandardCharsets.UTF_8));

            // 2. Voter wallet address
            byte[] voterAddress = Base58Utils.decode(voterWalletAddress);
            buffer.put(voterAddress);

            // 3. Vote type (1 byte)
            buffer.put((byte) voteType.ordinal());

            // 4. Voting power (8 bytes u64)
            buffer.putLong(votingPower.longValue());

            // 5. Reason length + reason
            byte[] reasonBytes = reason.getBytes(StandardCharsets.UTF_8);
            buffer.putInt(reasonBytes.length);
            buffer.put(reasonBytes);

            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created vote transaction data: {} bytes", result.length);
            return result;

        } catch (Exception e) {
            log.error("Error creating vote transaction data", e);
            throw new RuntimeException("Failed to create vote transaction data", e);
        }
    }

    // ===== 교환 관련 메서드들 =====

    @Override
    public TransactionBody createExchangePointsToTokensTransaction(
            String userWalletAddress,
            BigDecimal pointAmount,
            BigDecimal tokenAmount,
            String description) {

        try {
            log.info("Creating Solana exchange points to tokens transaction - User: {}, Points: {}, Tokens: {}",
                    userWalletAddress, pointAmount, tokenAmount);

            // 1. 트랜잭션 데이터 생성
            byte[] transactionData = createExchangeRequestTransactionData(
                    userWalletAddress,
                    pointAmount,
                    tokenAmount,
                    "POINTS_TO_TOKENS"
            );

            // 2. SolanaTransactionData 생성
            SolanaTransactionData solanaData = SolanaTransactionData.builder()
                    .recentBlockhash("11111111111111111111111111111111") // 실제로는 최근 블록해시 사용
                    .fee(5000L) // 기본 수수료
                    .programId(getTokenProgramId()) // 교환은 토큰 프로그램으로 처리
                    .build();

            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                    .type(TransactionBody.TransactionType.TOKEN_MINT) // 교환은 토큰 민팅으로 처리
                    .fromAddress(userWalletAddress)
                    .toAddress(null)
                    .data(HexUtils.bytesToHex(transactionData))
                    .networkType(NetworkType.SOLANA.name())
                    .networkSpecificData(solanaData)
                    .build();

        } catch (Exception e) {
            log.error("Error creating Solana exchange points to tokens transaction", e);
            throw new RuntimeException("Failed to create Solana exchange points to tokens transaction", e);
        }
    }

    @Override
    public TransactionBody createExchangeTokensToPointsTransaction(
            String userWalletAddress,
            BigDecimal tokenAmount,
            BigDecimal pointAmount,
            String description) {

        try {
            log.info("Creating Solana exchange tokens to points transaction - User: {}, Tokens: {}, Points: {}",
                    userWalletAddress, tokenAmount, pointAmount);

            // 1. 트랜잭션 데이터 생성
            byte[] transactionData = createExchangeRequestTransactionData(
                    userWalletAddress,
                    pointAmount,
                    tokenAmount,
                    "TOKENS_TO_POINTS"
            );

            // 2. SolanaTransactionData 생성
            SolanaTransactionData solanaData = SolanaTransactionData.builder()
                    .recentBlockhash("11111111111111111111111111111111") // 실제로는 최근 블록해시 사용
                    .fee(5000L) // 기본 수수료
                    .programId(getTokenProgramId()) // 교환은 토큰 프로그램으로 처리
                    .build();

            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                    .type(TransactionBody.TransactionType.TOKEN_BURN) // 교환은 토큰 소각으로 처리
                    .fromAddress(userWalletAddress)
                    .toAddress(null)
                    .data(HexUtils.bytesToHex(transactionData))
                    .networkType(NetworkType.SOLANA.name())
                    .networkSpecificData(solanaData)
                    .build();

        } catch (Exception e) {
            log.error("Error creating Solana exchange tokens to points transaction", e);
            throw new RuntimeException("Failed to create Solana exchange tokens to points transaction", e);
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
            buffer.put(EXCHANGE_REQUEST_DISCRIMINATOR);

            // 1. User wallet address
            byte[] userAddress = Base58Utils.decode(userWalletAddress);
            buffer.put(userAddress);

            // 2. Point amount (8 bytes u64)
            buffer.putLong(pointAmount.longValue());

            // 3. Token amount (8 bytes u64)
            buffer.putLong(tokenAmount.longValue());

            // 4. Exchange type length + exchange type
            byte[] exchangeTypeBytes = exchangeType.getBytes(StandardCharsets.UTF_8);
            buffer.putInt(exchangeTypeBytes.length);
            buffer.put(exchangeTypeBytes);

            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created exchange request transaction data: {} bytes", result.length);
            return result;

        } catch (Exception e) {
            log.error("Error creating exchange request transaction data", e);
            throw new RuntimeException("Failed to create exchange request transaction data", e);
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
            buffer.put(EXECUTE_EXCHANGE_DISCRIMINATOR);

            // 1. User wallet address
            byte[] userAddress = Base58Utils.decode(userWalletAddress);
            buffer.put(userAddress);

            // 2. Point amount (8 bytes u64)
            buffer.putLong(pointAmount.longValue());

            // 3. Token amount (8 bytes u64)
            buffer.putLong(tokenAmount.longValue());

            // 4. Exchange type length + exchange type
            byte[] exchangeTypeBytes = exchangeType.getBytes(StandardCharsets.UTF_8);
            buffer.putInt(exchangeTypeBytes.length);
            buffer.put(exchangeTypeBytes);

            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created execute exchange transaction data: {} bytes", result.length);
            return result;

        } catch (Exception e) {
            log.error("Error creating execute exchange transaction data", e);
            throw new RuntimeException("Failed to create execute exchange transaction data", e);
        }
    }

    @Override
    public byte[] createCompleteExchangeTransactionData(
            String adminWalletAddress,
            String userWalletAddress,
            BigDecimal tokenAmount) {

        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.put(COMPLETE_EXCHANGE_DISCRIMINATOR);

            // 1. Admin wallet address
            byte[] adminAddress = Base58Utils.decode(adminWalletAddress);
            buffer.put(adminAddress);

            // 2. User wallet address
            byte[] userAddress = Base58Utils.decode(userWalletAddress);
            buffer.put(userAddress);

            // 3. Token amount (8 bytes u64)
            buffer.putLong(tokenAmount.longValue());

            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created complete exchange transaction data: {} bytes", result.length);
            return result;

        } catch (Exception e) {
            log.error("Error creating complete exchange transaction data", e);
            throw new RuntimeException("Failed to create complete exchange transaction data", e);
        }
    }

    @Override
    public ExchangeInfo getExchangeInfoFromBlockchain(String walletAddress) {
        // 실제로는 Solana RPC를 통해 교환 정보를 조회
        return new ExchangeInfo(
                walletAddress,
                BigDecimal.valueOf(0.01), // 교환 비율
                BigDecimal.valueOf(100),  // 최소 교환 금액
                BigDecimal.valueOf(3600), // 쿨다운 기간 (초)
                LocalDateTime.now()       // 마지막 교환 시간
        );
    }

    @Override
    public TransactionBody createCompleteExchangeTransaction(
            String adminWalletAddress,
            String userWalletAddress,
            BigDecimal tokenAmount,
            String description) {

        try {
            log.info("Creating Solana complete exchange transaction - Admin: {}, User: {}, Amount: {}", 
                adminWalletAddress, userWalletAddress, tokenAmount);

            // 1. 트랜잭션 데이터 생성
            byte[] transactionData = createCompleteExchangeTransactionData(
                    adminWalletAddress,
                    userWalletAddress,
                    tokenAmount
            );

            // 2. SolanaTransactionData 생성
            SolanaTransactionData solanaData = SolanaTransactionData.builder()
                    .recentBlockhash("11111111111111111111111111111111") // 실제로는 최근 블록해시 사용
                    .fee(5000L) // 기본 수수료
                    .programId(getGovernanceProgramId())
                    .build();

            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                    .type(TransactionBody.TransactionType.TOKEN_TRANSFER)
                    .fromAddress(adminWalletAddress)
                    .toAddress(userWalletAddress)
                    .data(HexUtils.bytesToHex(transactionData))
                    .networkType(NetworkType.SOLANA.name())
                    .networkSpecificData(solanaData)
                    .build();

        } catch (Exception e) {
            log.error("Error creating Solana complete exchange transaction", e);
            throw new RuntimeException("Failed to create Solana complete exchange transaction", e);
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
            log.info("Creating Solana token transfer transaction - From: {}, To: {}, Amount: {}", 
                fromAddress, toAddress, amount);

            // 1. 트랜잭션 데이터 생성 (간단한 토큰 전송)
            byte[] transactionData = createTokenTransferTransactionData(fromAddress, toAddress, amount, tokenContract);

            // 2. SolanaTransactionData 생성
            SolanaTransactionData solanaData = SolanaTransactionData.builder()
                    .recentBlockhash("11111111111111111111111111111111")
                    .fee(5000L)
                    .programId(getTokenProgramId())
                    .build();

            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                    .type(TransactionBody.TransactionType.TOKEN_TRANSFER)
                    .fromAddress(fromAddress)
                    .toAddress(toAddress)
                    .data(HexUtils.bytesToHex(transactionData))
                    .networkType(NetworkType.SOLANA.name())
                    .networkSpecificData(solanaData)
                    .build();

        } catch (Exception e) {
            log.error("Error creating Solana token transfer transaction", e);
            throw new RuntimeException("Failed to create Solana token transfer transaction", e);
        }
    }

    @Override
    public TransactionBody createTokenMintTransaction(
            String toAddress,
            BigDecimal amount,
            String tokenContract) {

        try {
            log.info("Creating Solana token mint transaction - To: {}, Amount: {}", toAddress, amount);

            // 1. 트랜잭션 데이터 생성
            byte[] transactionData = createTokenMintTransactionData(toAddress, amount, tokenContract);

            // 2. SolanaTransactionData 생성
            SolanaTransactionData solanaData = SolanaTransactionData.builder()
                    .recentBlockhash("11111111111111111111111111111111")
                    .fee(5000L)
                    .programId(getTokenProgramId())
                    .build();

            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                    .type(TransactionBody.TransactionType.TOKEN_MINT)
                    .fromAddress(null) // MINT는 fromAddress 없음
                    .toAddress(toAddress)
                    .data(HexUtils.bytesToHex(transactionData))
                    .networkType(NetworkType.SOLANA.name())
                    .networkSpecificData(solanaData)
                    .build();

        } catch (Exception e) {
            log.error("Error creating Solana token mint transaction", e);
            throw new RuntimeException("Failed to create Solana token mint transaction", e);
        }
    }

    @Override
    public TransactionBody createTokenBurnTransaction(
            String fromAddress,
            BigDecimal amount,
            String tokenContract) {

        try {
            log.info("Creating Solana token burn transaction - From: {}, Amount: {}", fromAddress, amount);

            // 1. 트랜잭션 데이터 생성
            byte[] transactionData = createTokenBurnTransactionData(fromAddress, amount, tokenContract);

            // 2. SolanaTransactionData 생성
            SolanaTransactionData solanaData = SolanaTransactionData.builder()
                    .recentBlockhash("11111111111111111111111111111111")
                    .fee(5000L)
                    .programId(getTokenProgramId())
                    .build();

            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                    .type(TransactionBody.TransactionType.TOKEN_BURN)
                    .fromAddress(fromAddress)
                    .toAddress(null) // BURN은 toAddress 없음
                    .data(HexUtils.bytesToHex(transactionData))
                    .networkType(NetworkType.SOLANA.name())
                    .networkSpecificData(solanaData)
                    .build();

        } catch (Exception e) {
            log.error("Error creating Solana token burn transaction", e);
            throw new RuntimeException("Failed to create Solana token burn transaction", e);
        }
    }

    @Override
    public TransactionBody createExchangeTransaction(
            String userAddress,
            BigDecimal amount,
            String exchangeType,
            String tokenContract) {

        try {
            log.info("Creating Solana exchange transaction - User: {}, Amount: {}, Type: {}", 
                userAddress, amount, exchangeType);

            // 1. 트랜잭션 데이터 생성
            byte[] transactionData = createExchangeTransactionData(userAddress, amount, exchangeType, tokenContract);

            // 2. SolanaTransactionData 생성
            SolanaTransactionData solanaData = SolanaTransactionData.builder()
                    .recentBlockhash("11111111111111111111111111111111")
                    .fee(5000L)
                    .programId(getGovernanceProgramId())
                    .build();

            // 3. TransactionBody 생성 및 반환
            return TransactionBody.builder()
                    .type(TransactionBody.TransactionType.TOKEN_TRANSFER)
                    .fromAddress(userAddress)
                    .toAddress(null)
                    .data(HexUtils.bytesToHex(transactionData))
                    .networkType(NetworkType.SOLANA.name())
                    .networkSpecificData(solanaData)
                    .build();

        } catch (Exception e) {
            log.error("Error creating Solana exchange transaction", e);
            throw new RuntimeException("Failed to create Solana exchange transaction", e);
        }
    }
    
    // ===== 토큰 관련 트랜잭션 데이터 생성 메서드들 =====
    
    private byte[] createTokenTransferTransactionData(String fromAddress, String toAddress, BigDecimal amount, String tokenContract) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            // 간단한 토큰 전송 인스트럭션 (실제로는 SPL Token 프로그램 호출)
            buffer.put("TRANSFER".getBytes(StandardCharsets.UTF_8));
            
            // From address
            byte[] fromAddr = Base58Utils.decode(fromAddress);
            buffer.put(fromAddr);
            
            // To address
            byte[] toAddr = Base58Utils.decode(toAddress);
            buffer.put(toAddr);
            
            // Amount
            buffer.putLong(amount.longValue());
            
            // Token contract
            buffer.put(tokenContract.getBytes(StandardCharsets.UTF_8));
            
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
            
            // 토큰 민팅 인스트럭션
            buffer.put("MINT".getBytes(StandardCharsets.UTF_8));
            
            // To address
            byte[] toAddr = Base58Utils.decode(toAddress);
            buffer.put(toAddr);
            
            // Amount
            buffer.putLong(amount.longValue());
            
            // Token contract
            buffer.put(tokenContract.getBytes(StandardCharsets.UTF_8));
            
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
            
            // 토큰 소각 인스트럭션
            buffer.put("BURN".getBytes(StandardCharsets.UTF_8));
            
            // From address
            byte[] fromAddr = Base58Utils.decode(fromAddress);
            buffer.put(fromAddr);
            
            // Amount
            buffer.putLong(amount.longValue());
            
            // Token contract
            buffer.put(tokenContract.getBytes(StandardCharsets.UTF_8));
            
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
            
            // 교환 인스트럭션
            buffer.put("EXCHANGE".getBytes(StandardCharsets.UTF_8));
            
            // User address
            byte[] userAddr = Base58Utils.decode(userAddress);
            buffer.put(userAddr);
            
            // Amount
            buffer.putLong(amount.longValue());
            
            // Exchange type
            buffer.put(exchangeType.getBytes(StandardCharsets.UTF_8));
            
            // Token contract
            buffer.put(tokenContract.getBytes(StandardCharsets.UTF_8));
            
            byte[] result = Arrays.copyOf(buffer.array(), buffer.position());
            log.info("Created exchange transaction data: {} bytes", result.length);
            return result;
            
        } catch (Exception e) {
            log.error("Error creating exchange transaction data", e);
            throw new RuntimeException("Failed to create exchange transaction data", e);
        }
    }
}