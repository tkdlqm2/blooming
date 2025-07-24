package com.bloominggrace.governance.shared.blockchain.domain.model;

import lombok.Getter;
import lombok.Value;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;
import com.bloominggrace.governance.shared.blockchain.domain.model.TransactionRequest;

/**
 * 서명된 블록체인 트랜잭션
 * 제너릭 타입 T는 네트워크별 특정 데이터를 담습니다
 */
@Value
@Getter
public class SignedTransaction<T> {
    
    UUID signedTransactionId;     // 서명된 트랜잭션 고유 ID
    TransactionBody<T> transactionBody; // 원본 트랜잭션 본문
    byte[] signature;             // 서명 데이터
    String signerAddress;         // 서명자 주소
    LocalDateTime signedAt;       // 서명 시간
    String transactionHash;       // 블록체인 트랜잭션 해시 (브로드캐스트 후)
    TransactionStatus status;     // 트랜잭션 상태
    
    // Ethereum 서명 필드들
    BigInteger v;                 // 서명의 v 값
    BigInteger r;                 // 서명의 r 값
    BigInteger s;                 // 서명의 s 값
    
    /**
     * 트랜잭션 상태
     */
    public enum TransactionStatus {
        SIGNED,         // 서명됨 (아직 브로드캐스트 안됨)
        PENDING,        // 브로드캐스트됨 (블록체인에서 처리 중)
        CONFIRMED,      // 확인됨 (블록에 포함됨)
        FAILED,         // 실패
        CANCELLED       // 취소됨
    }
    
    /**
     * 서명된 트랜잭션 생성 (기본 생성자)
     */
    public SignedTransaction(
            TransactionBody<T> transactionBody,
            byte[] signature,
            String signerAddress) {
        this.signedTransactionId = UUID.randomUUID();
        this.transactionBody = transactionBody;
        this.signature = signature;
        this.signerAddress = signerAddress;
        this.signedAt = LocalDateTime.now();
        this.transactionHash = null; // 브로드캐스트 후 설정
        this.status = TransactionStatus.SIGNED;
        this.v = BigInteger.ZERO;
        this.r = BigInteger.ZERO;
        this.s = BigInteger.ZERO;
    }
    
    /**
     * Ethereum 서명된 트랜잭션 생성
     */
    public SignedTransaction(
            TransactionBody<T> transactionBody,
            BigInteger v,
            BigInteger r,
            BigInteger s,
            String signerAddress) {
        this.signedTransactionId = UUID.randomUUID();
        this.transactionBody = transactionBody;
        this.signature = new byte[0]; // Ethereum에서는 v, r, s 사용
        this.signerAddress = signerAddress;
        this.signedAt = LocalDateTime.now();
        this.transactionHash = null;
        this.status = TransactionStatus.SIGNED;
        this.v = v;
        this.r = r;
        this.s = s;
    }
    

    

    

    

    

} 