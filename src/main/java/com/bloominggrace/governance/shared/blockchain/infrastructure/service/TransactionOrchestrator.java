package com.bloominggrace.governance.shared.blockchain.infrastructure.service;

import com.bloominggrace.governance.blockchain.application.service.BlockchainClientFactory;
import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.blockchain.domain.model.TransactionBody;
import com.bloominggrace.governance.wallet.application.service.WalletApplicationService;
import com.bloominggrace.governance.wallet.application.service.WalletServiceFactory;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.domain.service.WalletService;
import com.bloominggrace.governance.shared.blockchain.domain.service.RawTransactionBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.utils.Numeric;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;
import org.springframework.context.ApplicationContext;

import java.math.BigInteger;

/**
 * 트랜잭션 실행 오케스트레이터
 * 역할: BlockchainGovernanceService를 통한 TransactionBody 생성 → 서명 → 브로드캐스트 전체 플로우 조율
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionOrchestrator {
    
    private final BlockchainClientFactory blockchainClientFactory;
    private final WalletServiceFactory walletServiceFactory;
    private final RawTransactionBuilderFactory rawTransactionBuilderFactory;
    // TransactionSigner와 TransactionBroadcaster는 WalletApplicationService를 통해 처리

    // ===== 거버넌스 관련 트랜잭션 실행 =====
    
    /**
     * 제안 생성 트랜잭션 실행 (RawTransaction 생성 → 서명 → 브로드캐스트)
     */
    public TransactionResult executeProposalCreation(
        UUID proposalId,
        String title,
        String description,
        String walletAddress,
        NetworkType networkType,
        BigDecimal proposalFee,
        LocalDateTime votingStartDate,
        LocalDateTime votingEndDate,
        BigDecimal requiredQuorum) {
        
        try {
            log.info("[Orchestrator] Starting executeProposalCreation - ProposalId: {}, Title: {}, Network: {}", 
                proposalId, title, networkType);
            
            // 1. RawTransaction 생성
            String rawTransactionJson = createProposalCreationRawTransaction(
                proposalId, title, description, walletAddress, networkType, 
                proposalFee, votingStartDate, votingEndDate, requiredQuorum
            );

            // 2. 지갑 정보 조회 및 개인키 복호화
            String decryptedPrivateKey = getDecryptedPrivateKey(walletAddress, networkType);

            // 3. 트랜잭션 서명
            byte[] signedTx = signTransaction(rawTransactionJson, walletAddress, networkType, decryptedPrivateKey);

            // 4. 블록체인에 브로드캐스트
            String txHash = broadcastTransaction(signedTx, networkType);

            // 5. 결과 반환
            if (txHash != null && !txHash.trim().isEmpty()) {
                return TransactionResult.success(UUID.randomUUID(), txHash, walletAddress, networkType.name(), "Create governance proposal: " + title);
            } else {
                return TransactionResult.failure(UUID.randomUUID(), walletAddress, networkType.name(), "No transaction hash returned from broadcast");
            }
        } catch (Exception e) {
            log.error("[Orchestrator] Failed proposal creation", e);
            return TransactionResult.failure(UUID.randomUUID(), walletAddress, networkType.name(), "Proposal creation failed: " + e.getMessage());
        }
    }
    
    /**
     * 투표 트랜잭션 실행 (RawTransaction 생성 → 서명 → 브로드캐스트)
     */
    public TransactionResult executeVoteCreation(
        BigInteger proposalCount,
        UUID proposalId,
        String walletAddress,
        String voteType,
        BigDecimal votingPower,
        String reason,
        NetworkType networkType) {
        
        try {
            log.info("[Orchestrator] Starting executeVoteCreation - ProposalId: {}, VoteType: {}, Network: {}", 
                proposalId, voteType, networkType);
            
            // 1. RawTransaction 생성
            String rawTransactionJson = createVoteRawTransaction(
                proposalCount, proposalId, walletAddress, networkType, voteType, reason, votingPower
            );

            // 2. 지갑 정보 조회 및 개인키 복호화
            String decryptedPrivateKey = getDecryptedPrivateKey(walletAddress, networkType);

            // 3. 트랜잭션 서명
            byte[] signedTx = signTransaction(rawTransactionJson, walletAddress, networkType, decryptedPrivateKey);

            // 4. 블록체인에 브로드캐스트
            String txHash = broadcastTransaction(signedTx, networkType);

            // 5. 결과 반환
            if (txHash != null && !txHash.trim().isEmpty()) {
                return TransactionResult.success(UUID.randomUUID(), txHash, walletAddress, networkType.name(), 
                    "Vote on proposal: " + proposalId + " - " + voteType);
            } else {
                return TransactionResult.failure(UUID.randomUUID(), walletAddress, networkType.name(), 
                    "No transaction hash returned from broadcast");
            }
        } catch (Exception e) {
            log.error("[Orchestrator] Failed vote creation", e);
            return TransactionResult.failure(UUID.randomUUID(), walletAddress, networkType.name(), 
                "Vote creation failed: " + e.getMessage());
        }
    }
    
    // ===== 토큰 관련 트랜잭션 실행 =====

    /**
     * 토큰 전송 트랜잭션 실행 (역할 분리: Builder → Sign → Broadcast)
     */
    public TransactionResult executeTransfer(
            String fromWalletAddress,
            String toWalletAddress,
            NetworkType networkType,
            BigDecimal amount,
            String tokenContract) {
        try {
            log.info("[Orchestrator] Starting executeTransfer - From: {}, To: {}, Network: {}, Amount: {}, Contract: {}", 
                fromWalletAddress, toWalletAddress, networkType, amount, tokenContract);
            
            // 1. RawTransaction 생성
            String rawTransactionJson = createRawTransaction(fromWalletAddress, toWalletAddress, networkType, amount, tokenContract);

            // 2. 지갑 정보 조회 및 개인키 복호화
            String decryptedPrivateKey = getDecryptedPrivateKey(fromWalletAddress, networkType);

            // 3. 트랜잭션 서명
            byte[] signedTx = signTransaction(rawTransactionJson, fromWalletAddress, networkType, decryptedPrivateKey);

            // 4. 블록체인에 브로드캐스트
            String txHash = broadcastTransaction(signedTx, networkType);

            // 5. 결과 반환
            if (txHash != null && !txHash.trim().isEmpty()) {
                return TransactionResult.success(UUID.randomUUID(), txHash, fromWalletAddress, networkType.name(), "ERC20 transfer: " + amount);
            } else {
                return TransactionResult.failure(UUID.randomUUID(), fromWalletAddress, networkType.name(), "No transaction hash returned from broadcast");
            }
        } catch (Exception e) {
            log.error("[Orchestrator] Failed ERC20 transfer", e);
            return TransactionResult.failure(UUID.randomUUID(), fromWalletAddress, networkType.name(), "ERC20 transfer failed: " + e.getMessage());
        }
    }

    /**
     * 투표권 위임 트랜잭션 실행 (RawTransaction 생성 → 서명 → 브로드캐스트)
     */
    public TransactionResult executeDelegationCreation(
        String delegatorWalletAddress,
        String delegateeWalletAddress,
        NetworkType networkType) {
        
        try {
            log.info("[Orchestrator] Starting executeDelegationCreation - Delegator: {}, Delegatee: {}, Network: {}",
                delegatorWalletAddress, delegateeWalletAddress, networkType);
            
            // 1. RawTransaction 생성
            String rawTransactionJson = createDelegationRawTransaction(
                delegatorWalletAddress, delegateeWalletAddress, networkType
            );

            // 2. 지갑 정보 조회 및 개인키 복호화
            String decryptedPrivateKey = getDecryptedPrivateKey(delegatorWalletAddress, networkType);

            // 3. 트랜잭션 서명
            byte[] signedTx = signTransaction(rawTransactionJson, delegatorWalletAddress, networkType, decryptedPrivateKey);

            // 4. 블록체인에 브로드캐스트
            String txHash = broadcastTransaction(signedTx, networkType);

            // 5. 결과 반환
            if (txHash != null && !txHash.trim().isEmpty()) {
                return TransactionResult.success(UUID.randomUUID(), txHash, delegatorWalletAddress, networkType.name(), "Delegate votes: " + delegateeWalletAddress);
            } else {
                return TransactionResult.failure(UUID.randomUUID(), delegatorWalletAddress, networkType.name(), "No transaction hash returned from broadcast");
            }
        } catch (Exception e) {
            log.error("[Orchestrator] Failed delegation creation", e);
            return TransactionResult.failure(UUID.randomUUID(), delegatorWalletAddress, networkType.name(), "Delegation creation failed: " + e.getMessage());
        }
    }

    private String createProposalCreationRawTransaction(
        UUID proposalId,
        String title,
        String description,
        String walletAddress,
        NetworkType networkType,
        BigDecimal proposalFee,
        LocalDateTime votingStartDate,
        LocalDateTime votingEndDate,
        BigDecimal requiredQuorum) {
        
        try {
            log.info("[Orchestrator] Creating proposal creation raw transaction - ProposalId: {}, Title: {}, Network: {}", 
                proposalId, title, networkType);
            
            // RawTransactionBuilder를 통한 RawTransaction 생성
            RawTransactionBuilder rawTransactionBuilder = rawTransactionBuilderFactory.getBuilder(networkType);
            String rawTransactionJson = rawTransactionBuilder.createProposalCreationRawTransaction(
                proposalId, title, description, walletAddress, proposalFee, 
                votingStartDate, votingEndDate, requiredQuorum, null
            );
            
            log.info("[Orchestrator] Created proposal creation raw transaction successfully");
            return rawTransactionJson;
            
        } catch (Exception e) {
            log.error("[Orchestrator] Failed to create proposal creation raw transaction", e);
            throw new RuntimeException("Failed to create proposal creation raw transaction: " + e.getMessage(), e);
        }
    }
    
    /**
     * 투표용 RawTransaction 생성
     */
    private String createVoteRawTransaction(
        BigInteger proposalCount,
        UUID proposalId,
        String walletAddress,
        NetworkType networkType,
        String voteType,
        String reason,
        BigDecimal votingPower) {
        
        try {
            RawTransactionBuilder rawTransactionBuilder = rawTransactionBuilderFactory.getBuilder(networkType);
            
            return rawTransactionBuilder.createVoteRawTransaction(
                proposalCount,
                proposalId,
                walletAddress,
                voteType,
                reason != null ? reason : "",
                votingPower,
                null // nonce는 RawTransactionBuilder에서 자동 조회
            );
        } catch (Exception e) {
            log.error("[Orchestrator] Failed to create vote raw transaction", e);
            throw new RuntimeException("Failed to create vote raw transaction: " + e.getMessage(), e);
        }
    }
    
    private String createRawTransaction(
        String fromWalletAddress,
        String toWalletAddress,
        NetworkType networkType,
        BigDecimal amount,
        String tokenContract) {

        // RawTransactionBuilder를 사용하여 RawTransaction 생성
        RawTransactionBuilder builder = rawTransactionBuilderFactory.getBuilder(networkType);
        Map<String, String> transactionData;
        // 트랜잭션 데이터를 Map으로 구성
        if (tokenContract != null && !tokenContract.trim().isEmpty()) {
            transactionData = Map.of(
                    "fromAddress", fromWalletAddress,
                    "toAddress", toWalletAddress,
                    "amount", amount.toString(),
                    "tokenAddress", tokenContract
            );
        } else {
            transactionData = Map.of(
                    "fromAddress", fromWalletAddress,
                    "toAddress", toWalletAddress,
                    "amount", amount.toString()
            );
        }

        String rawTransactionJson = builder.createRawTransaction(transactionData);
        log.info("[Orchestrator] Created RawTransaction using RawTransactionBuilder: {}", rawTransactionJson);

        return rawTransactionJson;
    }

    /**
     * 위임 RawTransaction 생성 (private 메서드)
     */
    private String createDelegationRawTransaction(
        String delegatorWalletAddress,
        String delegateeWalletAddress,
        NetworkType networkType) {
        
        try {
            log.info("[Orchestrator] Creating delegation RawTransaction - Delegator: {}, Delegatee: {}, Network: {}",
                delegatorWalletAddress, delegateeWalletAddress, networkType);

            // RawTransactionBuilder를 통해 위임 RawTransaction 생성
            RawTransactionBuilder rawTransactionBuilder = rawTransactionBuilderFactory.getBuilder(networkType);
            String rawTransactionJson = rawTransactionBuilder.createDelegationRawTransaction(
                delegatorWalletAddress,
                delegateeWalletAddress,
                networkType
            );
            
            log.info("[Orchestrator] Created delegation RawTransaction: {}", rawTransactionJson);
            return rawTransactionJson;
            
        } catch (Exception e) {
            log.error("[Orchestrator] Failed to create delegation RawTransaction", e);
            throw new RuntimeException("Failed to create delegation RawTransaction: " + e.getMessage(), e);
        }
    }
    
    private String getDecryptedPrivateKey(String fromWalletAddress, NetworkType networkType) {
        // 일반 지갑의 경우 WalletService를 통해 개인키 조회
        WalletService walletService = walletServiceFactory.getWalletService(networkType);
        return walletService.getDecryptedPrivateKey(fromWalletAddress);
    }
    
    private byte[] signTransaction(
            String rawTransactionJson,
            String fromWalletAddress,
            NetworkType networkType,
            String decryptedPrivateKey) {
        
        WalletService walletService = walletServiceFactory.getWalletService(networkType);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(rawTransactionJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse raw transaction JSON", e);
        }

        // RawTransaction JSON에서 nonce 추출
        long nonce = jsonNode.has("nonce") ? Long.parseLong(jsonNode.get("nonce").asText()) : 0L;
        log.info("[Orchestrator] Extracted nonce from RawTransaction JSON: {}", nonce);

        TransactionBody<Object> transactionBody =
            TransactionBody.builder()
                .type(TransactionBody.TransactionType.TOKEN_TRANSFER)
                .fromAddress(fromWalletAddress)
                .toAddress(jsonNode.get("toAddress").asText())
                .data(rawTransactionJson)
                .networkType(networkType.name())
                .build();

        byte[] signedTx = walletService.sign(transactionBody, decryptedPrivateKey);
        log.info("[Orchestrator] Signed transaction using signTransactionBody");
        
        return signedTx;
    }

    private String broadcastTransaction(byte[] signedTx, NetworkType networkType) {

        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
        
        String hexValue = Numeric.toHexString(signedTx);
        String txHash = blockchainClient.broadcastTransaction(hexValue);

        log.info("[Orchestrator] Transaction broadcasted, hash: {}", txHash);
        
        // txHash가 null인 경우 체크
        if (txHash == null || txHash.trim().isEmpty()) {
            log.error("[Orchestrator] Transaction hash is null or empty after broadcast");
            throw new RuntimeException("No transaction hash returned from blockchain broadcast");
        }
        
        return txHash;
    }

    // ===== 결과 클래스 =====
    
    public static class TransactionResult {
        private final UUID transactionId;
        private final String transactionHash;
        private final String walletAddress;
        private final String networkType;
        private final String description;
        private final boolean success;
        private final String errorMessage;
        
        private TransactionResult(UUID transactionId, String transactionHash, String walletAddress, 
                                String networkType, String description, boolean success, String errorMessage) {
            this.transactionId = transactionId;
            this.transactionHash = transactionHash;
            this.walletAddress = walletAddress;
            this.networkType = networkType;
            this.description = description;
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        public static TransactionResult success(UUID transactionId, String transactionHash, 
                                              String walletAddress, String networkType, String description) {
            return new TransactionResult(transactionId, transactionHash, walletAddress, networkType, description, true, null);
        }
        
        public static TransactionResult failure(UUID transactionId, String walletAddress, 
                                              String networkType, String errorMessage) {
            return new TransactionResult(transactionId, null, walletAddress, networkType, null, false, errorMessage);
        }
        
        // Getters
        public String getTransactionHash() { return transactionHash; }
        public String getWalletAddress() { return walletAddress; }
        public String getNetworkType() { return networkType; }
        public String getDescription() { return description; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }
} 