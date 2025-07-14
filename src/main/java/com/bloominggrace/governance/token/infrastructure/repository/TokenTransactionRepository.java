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
} 