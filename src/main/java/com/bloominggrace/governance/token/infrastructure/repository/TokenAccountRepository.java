package com.bloominggrace.governance.token.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.token.domain.model.TokenAccountId;

import java.util.List;
import java.util.Optional;

public interface TokenAccountRepository {
    TokenAccount save(TokenAccount tokenAccount);
    Optional<TokenAccount> findById(TokenAccountId id);
    Optional<TokenAccount> findByUserId(UserId userId);
    Optional<TokenAccount> findByWalletAddress(String walletAddress);
    List<TokenAccount> findAll();
    void delete(TokenAccountId id);
    boolean existsByUserId(UserId userId);
    boolean existsByWalletAddress(String walletAddress);
} 