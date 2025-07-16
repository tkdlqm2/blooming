package com.bloominggrace.governance.token.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenTransaction;
import com.bloominggrace.governance.token.domain.model.TokenTransactionId;
import com.bloominggrace.governance.token.domain.model.TokenTransactionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TokenTransactionRepository {
    TokenTransaction save(TokenTransaction tokenTransaction);
    Optional<TokenTransaction> findById(TokenTransactionId id);
    List<TokenTransaction> findByUserId(UserId userId);
    List<TokenTransaction> findByUserIdAndTransactionType(UserId userId, TokenTransactionType transactionType);
    List<TokenTransaction> findByWalletAddress(String walletAddress);
    List<TokenTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<TokenTransaction> findAll();
    void delete(TokenTransactionId id);
    
    /**
     * 사용자 ID로 트랜잭션을 생성일 기준 내림차순으로 조회
     */
    List<TokenTransaction> findByUserIdOrderByCreatedAtDesc(UserId userId);
    
    /**
     * 사용자 ID와 트랜잭션 타입으로 트랜잭션을 생성일 기준 내림차순으로 조회
     */
    List<TokenTransaction> findByUserIdAndTransactionTypeOrderByCreatedAtDesc(UserId userId, TokenTransactionType transactionType);
} 