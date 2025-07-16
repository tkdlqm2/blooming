package com.bloominggrace.governance.token.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public Optional<TokenAccount> findById(UUID id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public Optional<TokenAccount> findByUserId(UserId userId) {
        return jpaRepository.findByUserId(userId);
    }
    
    @Override
    public List<TokenAccount> findAllByUserId(UserId userId) {
        return jpaRepository.findAllByUserId(userId);
    }
    
    @Override
    public List<TokenAccount> findByWallet(Wallet wallet) {
        return jpaRepository.findByWallet(wallet);
    }
    
    @Override
    public List<TokenAccount> findByWalletAndNetwork(Wallet wallet, NetworkType network) {
        return jpaRepository.findByWalletAndNetwork(wallet, network);
    }
    
    @Override
    public Optional<TokenAccount> findByWalletAndContract(Wallet wallet, String contract) {
        return jpaRepository.findByWalletAndContract(wallet, contract);
    }
    
    @Override
    public List<TokenAccount> findByWalletAddress(String walletAddress) {
        return jpaRepository.findByWalletAddress(walletAddress);
    }
    
    @Override
    public List<TokenAccount> findByWalletAddressAndNetwork(String walletAddress, NetworkType network) {
        return jpaRepository.findByWalletAddressAndNetwork(walletAddress, network);
    }
    
    @Override
    public List<TokenAccount> findAll() {
        return jpaRepository.findAll();
    }
    
    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsByUserId(UserId userId) {
        return jpaRepository.existsByUserId(userId);
    }
    
    @Override
    public boolean existsByWalletAndContract(Wallet wallet, String contract) {
        return jpaRepository.existsByWalletAndContract(wallet, contract);
    }
} 