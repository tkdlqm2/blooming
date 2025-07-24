package com.bloominggrace.governance.token.application.service;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.token.domain.model.TokenAmount;
import com.bloominggrace.governance.token.infrastructure.repository.TokenAccountRepository;
import com.bloominggrace.governance.wallet.application.service.WalletApplicationService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.shared.domain.model.BlockchainTransactionType;
import com.bloominggrace.governance.shared.domain.model.Transaction;
import com.bloominggrace.governance.shared.infrastructure.repository.TransactionRepository;
import com.bloominggrace.governance.shared.infrastructure.service.TransactionOrchestrator;
import com.bloominggrace.governance.shared.infrastructure.service.TransactionOrchestrator.TransactionResult;
import com.bloominggrace.governance.shared.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import com.bloominggrace.governance.wallet.domain.model.Wallet;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenApplicationService {
    
    private final TokenAccountRepository tokenAccountRepository;
    private final WalletApplicationService walletApplicationService;
    private final TransactionRepository transactionRepository;
    private final TransactionOrchestrator transactionOrchestrator;
    
    /**
     * 토큰 계정 생성 또는 조회
     */
    public TokenAccount getOrCreateTokenAccount(UserId userId, String walletAddress, NetworkType network, String contract, String symbol) {
        // 먼저 특정 네트워크와 컨트랙트로 토큰 계정을 찾음
        Optional<TokenAccount> existingTokenAccount = tokenAccountRepository.findByWalletAddressAndNetworkAndContract(walletAddress, network, contract);
        
        if (existingTokenAccount.isPresent()) {
            return existingTokenAccount.get();
        }
        
        // 없으면 새로 생성
        Wallet wallet = walletApplicationService.getWalletByAddress(walletAddress)
            .orElseThrow(() -> new IllegalStateException("Wallet not found: " + walletAddress));
        
        TokenAccount newAccount = new TokenAccount(
            wallet,
            userId,
            network,
            contract,
            symbol
        );
        return tokenAccountRepository.save(newAccount);
    }
    // ===== 조회 메서드들 =====
    
    @Transactional(readOnly = true)
    public Optional<TokenAccount> getTokenAccount(UserId userId) {
        List<TokenAccount> accounts = tokenAccountRepository.findAllByUserId(userId);
        return accounts.isEmpty() ? Optional.empty() : Optional.of(accounts.get(0));
    }
    
    @Transactional(readOnly = true)
    public List<TokenAccount> getTokenAccountByWalletAddress(String walletAddress) {
        return tokenAccountRepository.findByWalletAddress(walletAddress);
    }
    
    @Transactional(readOnly = true)
    public List<TokenAccount> getAllTokenAccountsByUserId(UserId userId) {
        return tokenAccountRepository.findAllByUserId(userId);
    }
    
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionHistory(UserId userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
} 