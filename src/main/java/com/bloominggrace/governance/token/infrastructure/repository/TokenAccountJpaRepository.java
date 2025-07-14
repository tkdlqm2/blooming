package com.bloominggrace.governance.token.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.token.domain.model.TokenAccountId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenAccountJpaRepository extends JpaRepository<TokenAccount, TokenAccountId> {
    Optional<TokenAccount> findByUserId(UserId userId);
    Optional<TokenAccount> findByWalletAddress(String walletAddress);
    boolean existsByUserId(UserId userId);
    boolean existsByWalletAddress(String walletAddress);
} 