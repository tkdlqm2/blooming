package com.bloominggrace.governance.token.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.token.domain.model.TokenAccountId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TokenAccountRepositoryAdapter implements TokenAccountRepository {
    
    private final TokenAccountJpaRepository jpaRepository;
    
    public TokenAccountRepositoryAdapter(TokenAccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public TokenAccount save(TokenAccount tokenAccount) {
        return jpaRepository.save(tokenAccount);
    }
    
    @Override
    public Optional<TokenAccount> findById(TokenAccountId id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public Optional<TokenAccount> findByUserId(UserId userId) {
        return jpaRepository.findByUserId(userId);
    }
    
    @Override
    public Optional<TokenAccount> findByWalletAddress(String walletAddress) {
        return jpaRepository.findByWalletAddress(walletAddress);
    }
    
    @Override
    public List<TokenAccount> findAll() {
        return jpaRepository.findAll();
    }
    
    @Override
    public void delete(TokenAccountId id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsByUserId(UserId userId) {
        return jpaRepository.existsByUserId(userId);
    }
    
    @Override
    public boolean existsByWalletAddress(String walletAddress) {
        return jpaRepository.existsByWalletAddress(walletAddress);
    }
} 