package com.bloominggrace.governance.shared.domain.model;

import lombok.Getter;
import lombok.Value;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;
import com.bloominggrace.governance.shared.domain.model.TransactionRequest;

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
    
    /**
     * 브로드캐스트 후 트랜잭션 해시 설정
     */
    public SignedTransaction withTransactionHash(String transactionHash) {
        return new SignedTransaction(
            this.transactionBody,
            this.signature,
            this.signerAddress
        ).withStatus(TransactionStatus.PENDING);
    }
    
    /**
     * 트랜잭션 상태 업데이트
     */
    public SignedTransaction withStatus(TransactionStatus status) {
        return new SignedTransaction(
            this.transactionBody,
            this.signature,
            this.signerAddress
        );
    }
    
    /**
     * 서명된 트랜잭션을 바이트 배열로 변환 (브로드캐스트용)
     * 네트워크별로 다른 형식 사용
     */
    public byte[] toRawTransaction() {
        // 기본 구현 (기존 호환성)
        byte[] bodyBytes = transactionBody.toBytes();
        byte[] rawTransaction = new byte[bodyBytes.length + signature.length];
        
        System.arraycopy(bodyBytes, 0, rawTransaction, 0, bodyBytes.length);
        System.arraycopy(signature, 0, rawTransaction, bodyBytes.length, signature.length);
        
        return rawTransaction;
    }
    
    /**
     * 서명된 트랜잭션이 유효한지 확인
     */
    public boolean isValid() {
        return transactionBody != null && 
               signature != null && 
               signature.length > 0 && 
               signerAddress != null && 
               !signerAddress.isEmpty();
    }
    
    /**
     * 정책 제안 생성용 서명된 트랜잭션 생성
     */
    public static <T> SignedTransaction<T> createProposalTransaction(
            String fromAddress,
            String proposalId,
            String title,
            String description,
            String proposalFee,
            String networkType,
            byte[] signature,
            T networkSpecificData) {
        
        // 새로운 통합 메서드를 사용하여 TransactionBody 생성
        TransactionRequest.ProposalData proposalData = new TransactionRequest.ProposalData(
            proposalId, title, description, new java.math.BigDecimal(proposalFee)
        );
        
        // TransactionBodyFactory를 통해 생성 (간단한 구현)
        TransactionBody<T> transactionBody = new TransactionBody<>(
            TransactionBody.TransactionType.PROPOSAL_CREATE,
            fromAddress,
            null,
            proposalData.toString(),
            networkType,
            networkSpecificData
        );
        
        return new SignedTransaction<>(transactionBody, signature, fromAddress);
    }
    
    /**
     * 투표용 서명된 트랜잭션 생성
     */
    public static <T> SignedTransaction<T> createVoteTransaction(
            String fromAddress,
            String proposalId,
            String voteType,
            String votingPower,
            String reason,
            String networkType,
            byte[] signature,
            T networkSpecificData) {
        
        // 새로운 통합 메서드를 사용하여 TransactionBody 생성
        TransactionRequest.VoteData voteData = new TransactionRequest.VoteData(
            proposalId, voteType, new java.math.BigDecimal(votingPower), reason
        );
        
        // TransactionBodyFactory를 통해 생성 (간단한 구현)
        TransactionBody<T> transactionBody = new TransactionBody<>(
            TransactionBody.TransactionType.PROPOSAL_VOTE,
            fromAddress,
            null,
            voteData.toString(),
            networkType,
            networkSpecificData
        );
        
        return new SignedTransaction<>(transactionBody, signature, fromAddress);
    }
} 