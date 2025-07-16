package com.bloominggrace.governance.shared.domain.model;

import lombok.Value;

/**
 * 솔라나 네트워크별 트랜잭션 데이터
 */
@Value
public class SolanaTransactionData {
    String recentBlockhash;   // 최근 블록해시
    long fee;                 // 트랜잭션 수수료 (lamports)
    String programId;         // 프로그램 ID (선택적)
    
    public SolanaTransactionData(String recentBlockhash, long fee, String programId) {
        this.recentBlockhash = recentBlockhash;
        this.fee = fee;
        this.programId = programId;
    }
    
    public SolanaTransactionData(String recentBlockhash, long fee) {
        this(recentBlockhash, fee, null);
    }
    
    public static SolanaTransactionDataBuilder builder() {
        return new SolanaTransactionDataBuilder();
    }
    
    public static class SolanaTransactionDataBuilder {
        private String recentBlockhash;
        private long fee;
        private String programId;
        
        public SolanaTransactionDataBuilder recentBlockhash(String recentBlockhash) {
            this.recentBlockhash = recentBlockhash;
            return this;
        }
        
        public SolanaTransactionDataBuilder fee(long fee) {
            this.fee = fee;
            return this;
        }
        
        public SolanaTransactionDataBuilder programId(String programId) {
            this.programId = programId;
            return this;
        }
        
        public SolanaTransactionData build() {
            return new SolanaTransactionData(recentBlockhash, fee, programId);
        }
    }
} 