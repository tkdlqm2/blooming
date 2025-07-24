package com.bloominggrace.governance.shared.blockchain.domain.model;

import lombok.Value;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 트랜잭션 생성을 위한 요청 데이터
 * 공통 필드: fromAddress, toAddress, amount, tokenAddress, networkType
 * 트랜잭션별 고유 데이터: transactionData (Object)
 */
@Value
public class TransactionRequest {
    
    /**
     * 트랜잭션 타입
     */
    public enum TransactionType {
        PROPOSAL_CREATE,  // 정책 제안 생성
        PROPOSAL_VOTE,    // 정책 투표
        TOKEN_MINT,       // 토큰 민팅
        TOKEN_BURN,       // 토큰 소각
        TOKEN_TRANSFER    // 토큰 전송
    }
    
    // 공통 필드들
    String fromAddress;           // 발신자 주소
    String toAddress;             // 수신자 주소 (선택적)
    BigDecimal amount;            // 토큰 수량 (선택적)
    String tokenAddress;          // 토큰 주소 (선택적)
    String networkType;           // 네트워크 타입
    TransactionType type;         // 트랜잭션 타입
    BigInteger nonce;             // nonce (이더리움용, 선택적)
    
    // 트랜잭션별 고유 데이터 (Object로 추상화)
    Object transactionData;       // 트랜잭션 타입별 고유 데이터
    
    /**
     * 제안 생성용 요청 생성
     */
    public static TransactionRequest createProposalRequest(
            String fromAddress,
            String proposalId,
            String title,
            String description,
            BigDecimal proposalFee,
            String networkType) {
        
        ProposalData proposalData = ProposalData.builder()
            .proposalId(proposalId)
            .title(title)
            .description(description)
            .proposalFee(proposalFee)
            .build();
        
        return TransactionRequest.builder()
            .fromAddress(fromAddress)
            .networkType(networkType)
            .type(TransactionType.PROPOSAL_CREATE)
            .transactionData(proposalData)
            .build();
    }
    
    /**
     * 투표용 요청 생성
     */
    public static TransactionRequest createVoteRequest(
            String fromAddress,
            String proposalId,
            String voteType,
            BigDecimal votingPower,
            String reason,
            String networkType) {
        
        VoteData voteData = VoteData.builder()
            .proposalId(proposalId)
            .voteType(voteType)
            .votingPower(votingPower)
            .reason(reason)
            .build();
        
        return TransactionRequest.builder()
            .fromAddress(fromAddress)
            .networkType(networkType)
            .type(TransactionType.PROPOSAL_VOTE)
            .transactionData(voteData)
            .build();
    }
    
    /**
     * 토큰 전송용 요청 생성
     */
    public static TransactionRequest createTokenTransferRequest(
            String fromAddress,
            String toAddress,
            String tokenAddress,
            BigDecimal amount,
            String networkType) {
        
        TokenTransferData transferData = TokenTransferData.builder().build();
        
        return TransactionRequest.builder()
            .fromAddress(fromAddress)
            .toAddress(toAddress)
            .amount(amount)
            .tokenAddress(tokenAddress)
            .networkType(networkType)
            .type(TransactionType.TOKEN_TRANSFER)
            .transactionData(transferData)
            .build();
    }
    
    /**
     * 토큰 민팅용 요청 생성
     */
    public static TransactionRequest createTokenMintRequest(
            String fromAddress,
            BigDecimal amount,
            String description,
            String networkType) {
        
        TokenMintData mintData = TokenMintData.builder()
            .description(description)
            .build();
        
        return TransactionRequest.builder()
            .fromAddress(fromAddress)
            .amount(amount)
            .networkType(networkType)
            .type(TransactionType.TOKEN_MINT)
            .transactionData(mintData)
            .build();
    }
    
    /**
     * 토큰 소각용 요청 생성
     */
    public static TransactionRequest createTokenBurnRequest(
            String fromAddress,
            BigDecimal amount,
            String description,
            String networkType) {
        
        TokenBurnData burnData = TokenBurnData.builder()
            .description(description)
            .build();
        
        return TransactionRequest.builder()
            .fromAddress(fromAddress)
            .amount(amount)
            .networkType(networkType)
            .type(TransactionType.TOKEN_BURN)
            .transactionData(burnData)
            .build();
    }
    
    // ===== 트랜잭션별 데이터 클래스들 =====
    
    /**
     * 제안 생성 데이터
     */
    @Value
    public static class ProposalData {
        String proposalId;
        String title;
        String description;
        BigDecimal proposalFee;
        
        public static ProposalDataBuilder builder() {
            return new ProposalDataBuilder();
        }
        
        public static class ProposalDataBuilder {
            private String proposalId;
            private String title;
            private String description;
            private BigDecimal proposalFee;
            
            public ProposalDataBuilder proposalId(String proposalId) {
                this.proposalId = proposalId;
                return this;
            }
            
            public ProposalDataBuilder title(String title) {
                this.title = title;
                return this;
            }
            
            public ProposalDataBuilder description(String description) {
                this.description = description;
                return this;
            }
            
            public ProposalDataBuilder proposalFee(BigDecimal proposalFee) {
                this.proposalFee = proposalFee;
                return this;
            }
            
            public ProposalData build() {
                return new ProposalData(proposalId, title, description, proposalFee);
            }
        }
    }
    
    /**
     * 투표 데이터
     */
    @Value
    public static class VoteData {
        String proposalId;
        String voteType;
        BigDecimal votingPower;
        String reason;
        
        public static VoteDataBuilder builder() {
            return new VoteDataBuilder();
        }
        
        public static class VoteDataBuilder {
            private String proposalId;
            private String voteType;
            private BigDecimal votingPower;
            private String reason;
            
            public VoteDataBuilder proposalId(String proposalId) {
                this.proposalId = proposalId;
                return this;
            }
            
            public VoteDataBuilder voteType(String voteType) {
                this.voteType = voteType;
                return this;
            }
            
            public VoteDataBuilder votingPower(BigDecimal votingPower) {
                this.votingPower = votingPower;
                return this;
            }
            
            public VoteDataBuilder reason(String reason) {
                this.reason = reason;
                return this;
            }
            
            public VoteData build() {
                return new VoteData(proposalId, voteType, votingPower, reason);
            }
        }
    }
    
    /**
     * 토큰 전송 데이터
     */
    @Value
    public static class TokenTransferData {
        
        public static TokenTransferDataBuilder builder() {
            return new TokenTransferDataBuilder();
        }
        
        public static class TokenTransferDataBuilder {
            public TokenTransferData build() {
                return new TokenTransferData();
            }
        }
    }
    
    /**
     * 토큰 민팅 데이터
     */
    @Value
    public static class TokenMintData {
        String description;
        
        public static TokenMintDataBuilder builder() {
            return new TokenMintDataBuilder();
        }
        
        public static class TokenMintDataBuilder {
            private String description;
            
            public TokenMintDataBuilder description(String description) {
                this.description = description;
                return this;
            }
            
            public TokenMintData build() {
                return new TokenMintData(description);
            }
        }
    }
    
    /**
     * 토큰 소각 데이터
     */
    @Value
    public static class TokenBurnData {
        String description;
        
        public static TokenBurnDataBuilder builder() {
            return new TokenBurnDataBuilder();
        }
        
        public static class TokenBurnDataBuilder {
            private String description;
            
            public TokenBurnDataBuilder description(String description) {
                this.description = description;
                return this;
            }
            
            public TokenBurnData build() {
                return new TokenBurnData(description);
            }
        }
    }
    
    // ===== Builder 패턴 =====
    
    public static TransactionRequestBuilder builder() {
        return new TransactionRequestBuilder();
    }
    
    public static class TransactionRequestBuilder {
        private String fromAddress;
        private String toAddress;
        private BigDecimal amount;
        private String tokenAddress;
        private String networkType;
        private TransactionType type;
        private BigInteger nonce;
        private Object transactionData;
        
        public TransactionRequestBuilder fromAddress(String fromAddress) {
            this.fromAddress = fromAddress;
            return this;
        }
        
        public TransactionRequestBuilder toAddress(String toAddress) {
            this.toAddress = toAddress;
            return this;
        }
        
        public TransactionRequestBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }
        
        public TransactionRequestBuilder tokenAddress(String tokenAddress) {
            this.tokenAddress = tokenAddress;
            return this;
        }
        
        public TransactionRequestBuilder networkType(String networkType) {
            this.networkType = networkType;
            return this;
        }
        
        public TransactionRequestBuilder type(TransactionType type) {
            this.type = type;
            return this;
        }
        
        public TransactionRequestBuilder nonce(BigInteger nonce) {
            this.nonce = nonce;
            return this;
        }
        
        public TransactionRequestBuilder transactionData(Object transactionData) {
            this.transactionData = transactionData;
            return this;
        }
        
        public TransactionRequest build() {
            return new TransactionRequest(fromAddress, toAddress, amount, tokenAddress, networkType, type, nonce, transactionData);
        }
    }
} 