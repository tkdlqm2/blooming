package com.bloominggrace.governance.shared.domain.model;

import lombok.Getter;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 블록체인 트랜잭션의 본문 (서명되지 않은 트랜잭션 데이터)
 * 제너릭 타입 T는 네트워크별 특정 데이터를 담습니다
 */
@Value
@Getter
public class TransactionBody<T> {
    
    /**
     * 트랜잭션 타입 (기존 호환성을 위해 유지)
     * @deprecated BlockchainTransactionType 사용을 권장
     */
    @Deprecated
    public enum TransactionType {
        PROPOSAL_CREATE,  // 정책 제안 생성
        PROPOSAL_VOTE,    // 정책 투표
        TOKEN_MINT,       // 토큰 민팅
        TOKEN_BURN,       // 토큰 소각
        TOKEN_TRANSFER    // 토큰 전송
    }
    
    UUID transactionId;           // 트랜잭션 고유 ID
    TransactionType type;         // 트랜잭션 타입
    String fromAddress;           // 발신자 주소
    String toAddress;             // 수신자 주소 (선택적)
    String data;                  // 트랜잭션 데이터 (JSON 형태)
    LocalDateTime timestamp;      // 타임스탬프
    long nonce;                   // 논스 (트랜잭션 순서)
    String networkType;           // 네트워크 타입 (ETHEREUM, SOLANA)
    T networkSpecificData;        // 네트워크별 특정 데이터
    
    /**
     * 트랜잭션 본문 생성 (기본 생성자)
     */
    public TransactionBody(
            TransactionType type,
            String fromAddress,
            String toAddress,
            String data,
            String networkType,
            T networkSpecificData) {
        this.transactionId = UUID.randomUUID();
        this.type = type;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.nonce = System.currentTimeMillis(); // 기본값으로 타임스탬프 사용
        this.networkType = networkType;
        this.networkSpecificData = networkSpecificData;
    }
    
    /**
     * 트랜잭션 본문 생성 (nonce 포함 생성자)
     */
    public TransactionBody(
            TransactionType type,
            String fromAddress,
            String toAddress,
            String data,
            String networkType,
            T networkSpecificData,
            long nonce) {
        this.transactionId = UUID.randomUUID();
        this.type = type;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.nonce = nonce; // 전달받은 nonce 사용
        this.networkType = networkType;
        this.networkSpecificData = networkSpecificData;
    }
    
    /**
     * 트랜잭션 본문을 바이트 배열로 변환 (서명용 - 기존 호환성)
     */
    public byte[] toBytes() {
        String serialized = String.format("%s:%s:%s:%s:%s:%d:%s",
            transactionId.toString(),
            type.name(),
            fromAddress,
            toAddress != null ? toAddress : "",
            data,
            nonce,
            networkType
        );
        return serialized.getBytes();
    }
    
    /**
     * Builder 패턴을 위한 빌더 클래스
     */
    public static <T> TransactionBodyBuilder<T> builder() {
        return new TransactionBodyBuilder<>();
    }
    
    public static class TransactionBodyBuilder<T> {
        private TransactionType type;
        private String fromAddress;
        private String toAddress;
        private String data;
        private String networkType;
        private T networkSpecificData;
        
        public TransactionBodyBuilder<T> type(TransactionType type) {
            this.type = type;
            return this;
        }
        
        public TransactionBodyBuilder<T> fromAddress(String fromAddress) {
            this.fromAddress = fromAddress;
            return this;
        }
        
        public TransactionBodyBuilder<T> toAddress(String toAddress) {
            this.toAddress = toAddress;
            return this;
        }
        
        public TransactionBodyBuilder<T> data(String data) {
            this.data = data;
            return this;
        }
        
        public TransactionBodyBuilder<T> networkType(String networkType) {
            this.networkType = networkType;
            return this;
        }
        
        public TransactionBodyBuilder<T> networkSpecificData(T networkSpecificData) {
            this.networkSpecificData = networkSpecificData;
            return this;
        }
        
        public TransactionBody<T> build() {
            return new TransactionBody<>(type, fromAddress, toAddress, data, networkType, networkSpecificData);
        }
    }
} 