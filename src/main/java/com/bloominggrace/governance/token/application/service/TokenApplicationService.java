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
import com.bloominggrace.governance.shared.domain.model.TransactionBody;
import com.bloominggrace.governance.shared.domain.model.TransactionRequest;
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
    private final DomainEventPublisher eventPublisher;
    
    /**
     * 토큰 계정 생성 또는 조회
     */
    public TokenAccount getOrCreateTokenAccount(UserId userId, String walletAddress, NetworkType network, String contract, String symbol) {
        return tokenAccountRepository.findByWalletAddress(walletAddress)
            .stream().findFirst()
            .orElseGet(() -> {
                // Wallet 객체를 찾아야 함
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
            });
    }
    
    /**
     * 토큰 발행 (Mint)
     */
    public String mintTokens(
            UserId userId,
            String walletAddress,
            BigDecimal amount,
            String description,
            String networkType) {
        
        NetworkType network = NetworkType.valueOf(networkType.toUpperCase());
        TokenAccount tokenAccount = getOrCreateTokenAccount(userId, walletAddress, network, "default-contract", "TOKEN");
        
        // 블록체인 트랜잭션 생성
        TransactionRequest.TokenMintData mintData = new TransactionRequest.TokenMintData(
            description
        );
        TransactionBody txBody = walletApplicationService.createTransactionBody(
            walletAddress,
            null,
            amount,
            null,
            mintData,
            TransactionRequest.TransactionType.TOKEN_MINT,
            network
        );
        
        byte[] signedRawTransaction = walletApplicationService.signTransactionBody(txBody, walletAddress);
        String transactionSignature = walletApplicationService.broadcastSignedTransaction(signedRawTransaction, networkType);
        
        // 토큰 발행
        tokenAccount.mintTokens(amount, description);
        tokenAccountRepository.save(tokenAccount);
        
        // 트랜잭션 기록
        Transaction transaction = new Transaction(
            userId,
            BlockchainTransactionType.TOKEN_MINT,
            network,
            amount,
            walletAddress,
            null,
            description
        );
        transaction.confirm(transactionSignature);
        transactionRepository.save(transaction);
        
        return transactionSignature;
    }
    
    /**
     * 토큰 소각 (Burn)
     */
    public String burnTokens(
            UserId userId,
            String walletAddress,
            BigDecimal amount,
            String description,
            String networkType) {
        
        NetworkType network = NetworkType.valueOf(networkType.toUpperCase());
        TokenAccount tokenAccount = getOrCreateTokenAccount(userId, walletAddress, network, "default-contract", "TOKEN");
        
        if (tokenAccount.getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("소각할 토큰이 부족합니다. 필요: " + amount + ", 보유: " + tokenAccount.getAvailableBalance());
        }
        
        // 블록체인 트랜잭션 생성
        TransactionRequest.TokenBurnData burnData = new TransactionRequest.TokenBurnData(
            description
        );
        TransactionBody txBody = walletApplicationService.createTransactionBody(
            walletAddress,
            null,
            amount,
            null,
            burnData,
            TransactionRequest.TransactionType.TOKEN_BURN,
            network
        );
        
        byte[] signedRawTransaction = walletApplicationService.signTransactionBody(txBody, walletAddress);
        String transactionSignature = walletApplicationService.broadcastSignedTransaction(signedRawTransaction, networkType);
        
        // 토큰 소각
        tokenAccount.burnTokens(amount, description);
        tokenAccountRepository.save(tokenAccount);
        
        // 트랜잭션 기록
        Transaction transaction = new Transaction(
            userId,
            BlockchainTransactionType.TOKEN_BURN,
            network,
            amount,
            walletAddress,
            null,
            description
        );
        transaction.confirm(transactionSignature);
        transactionRepository.save(transaction);
        
        return transactionSignature;
    }
    
    /**
     * 토큰 전송
     */
    public String transferTokens(
            UserId fromUserId,
            String fromWalletAddress,
            String toWalletAddress,
            BigDecimal amount,
            String description,
            String networkType) {
        
        NetworkType network = NetworkType.valueOf(networkType.toUpperCase());
        TokenAccount fromAccount = getOrCreateTokenAccount(fromUserId, fromWalletAddress, network, "default-contract", "TOKEN");
        
        if (fromAccount.getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("전송할 토큰이 부족합니다. 필요: " + amount + ", 보유: " + fromAccount.getAvailableBalance());
        }
        
        // 블록체인 트랜잭션 생성
        TransactionRequest.TokenTransferData transferData = new TransactionRequest.TokenTransferData();
        TransactionBody txBody = walletApplicationService.createTransactionBody(
            fromWalletAddress,
            toWalletAddress,
            amount,
            null,
            transferData,
            TransactionRequest.TransactionType.TOKEN_TRANSFER,
            network
        );
        
        byte[] signedRawTransaction = walletApplicationService.signTransactionBody(txBody, fromWalletAddress);
        String transactionSignature = walletApplicationService.broadcastSignedTransaction(signedRawTransaction, networkType);
        
        // 토큰 전송
        fromAccount.transferTokens(amount, description);
        tokenAccountRepository.save(fromAccount);
        
        // 수신자 계정 생성 또는 업데이트
        TokenAccount toAccount = getOrCreateTokenAccount(null, toWalletAddress, network, "default-contract", "TOKEN"); // 수신자는 userId가 없을 수 있음
        toAccount.receiveTokens(amount, description);
        tokenAccountRepository.save(toAccount);
        
        // 트랜잭션 기록
        Transaction transaction = new Transaction(
            fromUserId,
            BlockchainTransactionType.TOKEN_TRANSFER,
            network,
            amount,
            fromWalletAddress,
            toWalletAddress,
            description
        );
        transaction.confirm(transactionSignature);
        transactionRepository.save(transaction);
        
        return transactionSignature;
    }
    
    /**
     * 토큰 스테이킹
     */
    public String stakeTokens(
            UserId userId,
            String walletAddress,
            BigDecimal amount,
            String description,
            String networkType) {
        
        NetworkType network = NetworkType.valueOf(networkType.toUpperCase());
        TokenAccount tokenAccount = getOrCreateTokenAccount(userId, walletAddress, network, "default-contract", "TOKEN");
        
        if (tokenAccount.getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("스테이킹할 토큰이 부족합니다. 필요: " + amount + ", 보유: " + tokenAccount.getAvailableBalance());
        }
        
        // 블록체인 트랜잭션 생성
        TransactionRequest.TokenStakeData stakeData = new TransactionRequest.TokenStakeData();
        TransactionBody txBody = walletApplicationService.createTransactionBody(
            walletAddress,
            null,
            amount,
            null,
            stakeData,
            TransactionRequest.TransactionType.TOKEN_STAKE,
            network
        );
        
        byte[] signedRawTransaction = walletApplicationService.signTransactionBody(txBody, walletAddress);
        String transactionSignature = walletApplicationService.broadcastSignedTransaction(signedRawTransaction, networkType);
        
        // 토큰 스테이킹
        tokenAccount.stakeTokens(amount);
        tokenAccountRepository.save(tokenAccount);
        
        // 트랜잭션 기록
        Transaction transaction = new Transaction(
            userId,
            BlockchainTransactionType.TOKEN_STAKE,
            network,
            amount,
            walletAddress,
            null,
            description
        );
        transaction.confirm(transactionSignature);
        transactionRepository.save(transaction);
        
        return transactionSignature;
    }
    
    /**
     * 토큰 언스테이킹
     */
    public String unstakeTokens(
            UserId userId,
            String walletAddress,
            BigDecimal amount,
            String description,
            String networkType) {
        
        NetworkType network = NetworkType.valueOf(networkType.toUpperCase());
        TokenAccount tokenAccount = getOrCreateTokenAccount(userId, walletAddress, network, "default-contract", "TOKEN");
        
        if (tokenAccount.getStakedBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("언스테이킹할 토큰이 부족합니다. 필요: " + amount + ", 스테이킹된: " + tokenAccount.getStakedBalance());
        }
        
        // 블록체인 트랜잭션 생성
        TransactionRequest.TokenUnstakeData unstakeData = new TransactionRequest.TokenUnstakeData();
        TransactionBody txBody = walletApplicationService.createTransactionBody(
            walletAddress,
            null,
            amount,
            null,
            unstakeData,
            TransactionRequest.TransactionType.TOKEN_UNSTAKE,
            network
        );
        
        byte[] signedRawTransaction = walletApplicationService.signTransactionBody(txBody, walletAddress);
        String transactionSignature = walletApplicationService.broadcastSignedTransaction(signedRawTransaction, networkType);
        
        // 토큰 언스테이킹
        tokenAccount.unstakeTokens(amount);
        tokenAccountRepository.save(tokenAccount);
        
        // 트랜잭션 기록
        Transaction transaction = new Transaction(
            userId,
            BlockchainTransactionType.TOKEN_UNSTAKE,
            network,
            amount,
            walletAddress,
            null,
            description
        );
        transaction.confirm(transactionSignature);
        transactionRepository.save(transaction);
        
        return transactionSignature;
    }
    
    /**
     * 투표권 계산 (스테이킹된 토큰 기반)
     */
    public long calculateVotingPower(UserId userId) {
        List<TokenAccount> accounts = tokenAccountRepository.findAllByUserId(userId);
        return accounts.stream()
            .mapToLong(account -> account.getStakedBalance().longValue())
            .sum();
    }
    
    /**
     * 지갑 주소로 네트워크 타입 결정
     */
    public NetworkType determineNetworkType(String walletAddress) {
        return com.bloominggrace.governance.shared.util.AddressUtils.guessNetworkType(walletAddress);
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