package com.bloominggrace.governance.shared.domain.model;

/**
 * 블록체인 트랜잭션 타입
 * 실제 블록체인 네트워크에 발행되는 트랜잭션들의 유형
 */
public enum BlockchainTransactionType {
    // 거버넌스 관련 블록체인 트랜잭션
    PROPOSAL_CREATE("정책 제안 생성"),
    PROPOSAL_VOTE("정책 투표"),
    
    // 토큰 관련 블록체인 트랜잭션
    TOKEN_MINT("토큰 민팅"),
    TOKEN_BURN("토큰 소각"),
    TOKEN_TRANSFER("토큰 전송");

    private final String description;

    BlockchainTransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 