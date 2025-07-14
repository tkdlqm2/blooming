package com.bloominggrace.governance.token.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenTransaction;
import com.bloominggrace.governance.token.domain.model.TokenTransactionId;
import com.bloominggrace.governance.token.domain.model.TokenTransactionType;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TokenTransactionRepositoryAdapter implements TokenTransactionRepository {
    
    private final TokenTransactionJpaRepository jpaRepository;
    
    public TokenTransactionRepositoryAdapter(TokenTransactionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public TokenTransaction save(TokenTransaction tokenTransaction) {
        return jpaRepository.save(tokenTransaction);
    }
    
    @Override
    public Optional<TokenTransaction> findById(TokenTransactionId id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public List<TokenTransaction> findByUserId(UserId userId) {
        return jpaRepository.findByUserId(userId);
    }
    
    @Override
    public List<TokenTransaction> findByUserIdAndTransactionType(UserId userId, TokenTransactionType transactionType) {
        return jpaRepository.findByUserIdAndTransactionType(userId, transactionType);
    }
    
    @Override
    public List<TokenTransaction> findByWalletAddress(String walletAddress) {
        return jpaRepository.findByWalletAddress(walletAddress);
    }
    
    @Override
    public List<TokenTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByCreatedAtBetween(startDate, endDate);
    }
    
    @Override
    public List<TokenTransaction> findAll() {
        return jpaRepository.findAll();
    }
    
    @Override
    public void delete(TokenTransactionId id) {
        jpaRepository.deleteById(id);
    }
} 