package com.bloominggrace.governance.token.application.service;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.token.domain.model.TokenAmount;
import com.bloominggrace.governance.token.domain.model.TokenTransaction;
import com.bloominggrace.governance.token.domain.model.TokenTransactionType;
import com.bloominggrace.governance.token.domain.service.TokenManagementService;
import com.bloominggrace.governance.token.infrastructure.repository.TokenAccountRepository;
import com.bloominggrace.governance.token.infrastructure.repository.TokenTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenApplicationService {
    
    private final TokenAccountRepository tokenAccountRepository;
    private final TokenTransactionRepository tokenTransactionRepository;
    private final TokenManagementService tokenManagementService;

    public TokenAccount createTokenAccount(UserId userId, String walletAddress, String blockchainType) {
        return tokenManagementService.createTokenAccount(userId, walletAddress);
    }

    public String mintTokens(UserId userId, TokenAmount amount, String description) {
        return tokenManagementService.mintTokens(userId, amount, description);
    }

    public String stakeTokens(UserId userId, TokenAmount amount) {
        return tokenManagementService.stakeTokens(userId, amount);
    }

    public String unstakeTokens(UserId userId, TokenAmount amount) {
        return tokenManagementService.unstakeTokens(userId, amount);
    }

    public String transferTokens(UserId fromUserId, String toWalletAddress, TokenAmount amount, String description) {
        return tokenManagementService.transferTokens(fromUserId, toWalletAddress, amount, description);
    }

    public String burnTokens(UserId userId, TokenAmount amount, String description) {
        return tokenManagementService.burnTokens(userId, amount, description);
    }

    public Optional<TokenAccount> getTokenAccount(UserId userId) {
        return tokenManagementService.getTokenAccount(userId);
    }

    public Optional<TokenAccount> getTokenAccountByWalletAddress(String walletAddress) {
        return tokenManagementService.getTokenAccountByWalletAddress(walletAddress);
    }

    public List<TokenTransaction> getTransactionHistory(UserId userId) {
        return tokenManagementService.getTransactionHistory(userId);
    }

    public List<TokenTransaction> getTransactionHistoryByType(UserId userId, TokenTransactionType transactionType) {
        return tokenManagementService.getTransactionHistoryByType(userId, transactionType);
    }

    public void deactivateTokenAccount(UserId userId) {
        tokenManagementService.deactivateTokenAccount(userId);
    }

    public void activateTokenAccount(UserId userId) {
        tokenManagementService.activateTokenAccount(userId);
    }

    public boolean isValidWalletAddress(String walletAddress) {
        return tokenManagementService.isValidWalletAddress(walletAddress);
    }

    public TokenAmount getBalance(String walletAddress) {
        return tokenManagementService.getBalance(walletAddress);
    }

    public TokenAmount getStakedBalance(String walletAddress) {
        return tokenManagementService.getStakedBalance(walletAddress);
    }
} 