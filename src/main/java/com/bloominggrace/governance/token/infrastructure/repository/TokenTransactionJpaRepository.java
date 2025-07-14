package com.bloominggrace.governance.token.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenTransaction;
import com.bloominggrace.governance.token.domain.model.TokenTransactionId;
import com.bloominggrace.governance.token.domain.model.TokenTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TokenTransactionJpaRepository extends JpaRepository<TokenTransaction, TokenTransactionId> {
    List<TokenTransaction> findByUserId(UserId userId);
    List<TokenTransaction> findByUserIdAndTransactionType(UserId userId, TokenTransactionType transactionType);
    List<TokenTransaction> findByWalletAddress(String walletAddress);
    List<TokenTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
} 