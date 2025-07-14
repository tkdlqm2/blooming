package com.bloominggrace.governance.token.domain.service;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.token.domain.model.TokenAmount;
import com.bloominggrace.governance.token.domain.model.TokenTransaction;
import com.bloominggrace.governance.token.domain.model.TokenTransactionType;
import com.bloominggrace.governance.token.infrastructure.repository.TokenAccountRepository;
import com.bloominggrace.governance.token.infrastructure.repository.TokenTransactionRepository;
import com.bloominggrace.governance.wallet.application.service.BlockchainClientFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenManagementService {
    
    private final TokenAccountRepository tokenAccountRepository;
    private final TokenTransactionRepository tokenTransactionRepository;
    private final BlockchainClientFactory blockchainClientFactory;
    
    /**
     * 토큰 계정 생성
     */
    public TokenAccount createTokenAccount(UserId userId, String walletAddress) {
        // 지갑 주소 유효성 검증
        if (!isValidWalletAddress(walletAddress)) {
            throw new IllegalArgumentException("Invalid wallet address: " + walletAddress);
        }
        
        // 이미 존재하는 계정인지 확인
        if (tokenAccountRepository.existsByWalletAddress(walletAddress)) {
            throw new IllegalStateException("Token account already exists for wallet: " + walletAddress);
        }
        
        TokenAccount tokenAccount = new TokenAccount(userId, walletAddress);
        return tokenAccountRepository.save(tokenAccount);
    }
    
    /**
     * 토큰 민팅
     */
    public String mintTokens(UserId userId, TokenAmount amount, String description) {
        TokenAccount account = getTokenAccount(userId)
            .orElseThrow(() -> new IllegalArgumentException("Token account not found for user: " + userId));
        
        // 블록체인에서 민팅 실행
        String networkType = getNetworkTypeFromWalletAddress(account.getWalletAddress());
        String transactionHash = blockchainClientFactory.getBlockchainClient(networkType)
            .mintToken(account.getWalletAddress(), amount.getAmount(), description);
        
        // 로컬 데이터베이스 업데이트
        account.mintTokens(amount, description);
        tokenAccountRepository.save(account);
        
        // 트랜잭션 기록
        TokenTransaction transaction = new TokenTransaction(
            userId,
            account.getWalletAddress(),
            TokenTransactionType.MINT,
            amount,
            description
        );
        transaction.confirm(transactionHash);
        tokenTransactionRepository.save(transaction);
        
        return transactionHash;
    }
    
    /**
     * 토큰 스테이킹
     */
    public String stakeTokens(UserId userId, TokenAmount amount) {
        TokenAccount account = getTokenAccount(userId)
            .orElseThrow(() -> new IllegalArgumentException("Token account not found for user: " + userId));
        
        if (!account.hasAvailableBalance(amount)) {
            throw new IllegalStateException("Insufficient available balance for staking");
        }
        
        // 블록체인에서 스테이킹 실행
        String networkType = getNetworkTypeFromWalletAddress(account.getWalletAddress());
        String transactionHash = blockchainClientFactory.getBlockchainClient(networkType)
            .stakeToken(account.getWalletAddress(), amount.getAmount());
        
        // 로컬 데이터베이스 업데이트
        account.stakeTokens(amount);
        tokenAccountRepository.save(account);
        
        // 트랜잭션 기록
        TokenTransaction transaction = new TokenTransaction(
            userId,
            account.getWalletAddress(),
            TokenTransactionType.STAKE,
            amount,
            "Staking tokens"
        );
        transaction.confirm(transactionHash);
        tokenTransactionRepository.save(transaction);
        
        return transactionHash;
    }
    
    /**
     * 토큰 언스테이킹
     */
    public String unstakeTokens(UserId userId, TokenAmount amount) {
        TokenAccount account = getTokenAccount(userId)
            .orElseThrow(() -> new IllegalArgumentException("Token account not found for user: " + userId));
        
        if (!account.hasStakedBalance(amount)) {
            throw new IllegalStateException("Insufficient staked balance for unstaking");
        }
        
        // 블록체인에서 언스테이킹 실행
        String networkType = getNetworkTypeFromWalletAddress(account.getWalletAddress());
        String transactionHash = blockchainClientFactory.getBlockchainClient(networkType)
            .unstakeToken(account.getWalletAddress(), amount.getAmount());
        
        // 로컬 데이터베이스 업데이트
        account.unstakeTokens(amount);
        tokenAccountRepository.save(account);
        
        // 트랜잭션 기록
        TokenTransaction transaction = new TokenTransaction(
            userId,
            account.getWalletAddress(),
            TokenTransactionType.UNSTAKE,
            amount,
            "Unstaking tokens"
        );
        transaction.confirm(transactionHash);
        tokenTransactionRepository.save(transaction);
        
        return transactionHash;
    }
    
    /**
     * 토큰 전송
     */
    public String transferTokens(UserId fromUserId, String toWalletAddress, TokenAmount amount, String description) {
        TokenAccount fromAccount = getTokenAccount(fromUserId)
            .orElseThrow(() -> new IllegalArgumentException("Token account not found for user: " + fromUserId));
        
        if (!isValidWalletAddress(toWalletAddress)) {
            throw new IllegalArgumentException("Invalid destination wallet address: " + toWalletAddress);
        }
        
        if (!fromAccount.hasAvailableBalance(amount)) {
            throw new IllegalStateException("Insufficient available balance for transfer");
        }
        
        // 블록체인에서 전송 실행
        String networkType = getNetworkTypeFromWalletAddress(fromAccount.getWalletAddress());
        String transactionHash = blockchainClientFactory.getBlockchainClient(networkType)
            .transferToken(fromAccount.getWalletAddress(), toWalletAddress, amount.getAmount(), description);
        
        // 송금자 계정 업데이트
        fromAccount.transferTokens(amount, description);
        tokenAccountRepository.save(fromAccount);
        
        // 수신자 계정 업데이트 (존재하는 경우)
        Optional<TokenAccount> toAccount = getTokenAccountByWalletAddress(toWalletAddress);
        if (toAccount.isPresent()) {
            toAccount.get().mintTokens(amount, "Received from transfer");
            tokenAccountRepository.save(toAccount.get());
        }
        
        // 트랜잭션 기록
        TokenTransaction transaction = new TokenTransaction(
            fromUserId,
            fromAccount.getWalletAddress(),
            TokenTransactionType.TRANSFER,
            amount,
            description
        );
        transaction.confirm(transactionHash);
        tokenTransactionRepository.save(transaction);
        
        return transactionHash;
    }
    
    /**
     * 토큰 소각
     */
    public String burnTokens(UserId userId, TokenAmount amount, String description) {
        TokenAccount account = getTokenAccount(userId)
            .orElseThrow(() -> new IllegalArgumentException("Token account not found for user: " + userId));
        
        if (!account.hasAvailableBalance(amount)) {
            throw new IllegalStateException("Insufficient available balance for burning");
        }
        
        // 블록체인에서 소각 실행
        String networkType = getNetworkTypeFromWalletAddress(account.getWalletAddress());
        String transactionHash = blockchainClientFactory.getBlockchainClient(networkType)
            .burnToken(account.getWalletAddress(), amount.getAmount(), description);
        
        // 로컬 데이터베이스 업데이트
        account.burnTokens(amount, description);
        tokenAccountRepository.save(account);
        
        // 트랜잭션 기록
        TokenTransaction transaction = new TokenTransaction(
            userId,
            account.getWalletAddress(),
            TokenTransactionType.BURN,
            amount,
            description
        );
        transaction.confirm(transactionHash);
        tokenTransactionRepository.save(transaction);
        
        return transactionHash;
    }
    
    /**
     * 토큰 계정 조회
     */
    public Optional<TokenAccount> getTokenAccount(UserId userId) {
        // 사용자 ID로 토큰 계정을 찾는 로직 구현
        // 현재는 지갑 주소로만 조회 가능하므로, 사용자와 지갑의 관계를 통해 조회해야 함
        return Optional.empty(); // TODO: 사용자 ID로 토큰 계정 조회 구현
    }
    
    /**
     * 지갑 주소로 토큰 계정 조회
     */
    public Optional<TokenAccount> getTokenAccountByWalletAddress(String walletAddress) {
        return tokenAccountRepository.findByWalletAddress(walletAddress);
    }
    
    /**
     * 트랜잭션 히스토리 조회
     */
    public List<TokenTransaction> getTransactionHistory(UserId userId) {
        // 사용자 ID로 트랜잭션 히스토리를 찾는 로직 구현
        return List.of(); // TODO: 사용자 ID로 트랜잭션 히스토리 조회 구현
    }
    
    /**
     * 트랜잭션 타입별 히스토리 조회
     */
    public List<TokenTransaction> getTransactionHistoryByType(UserId userId, TokenTransactionType transactionType) {
        // 사용자 ID와 트랜잭션 타입으로 히스토리를 찾는 로직 구현
        return List.of(); // TODO: 사용자 ID와 트랜잭션 타입으로 히스토리 조회 구현
    }
    
    /**
     * 토큰 계정 비활성화
     */
    public void deactivateTokenAccount(UserId userId) {
        TokenAccount account = getTokenAccount(userId)
            .orElseThrow(() -> new IllegalArgumentException("Token account not found for user: " + userId));
        
        account.deactivate();
        tokenAccountRepository.save(account);
    }
    
    /**
     * 토큰 계정 활성화
     */
    public void activateTokenAccount(UserId userId) {
        TokenAccount account = getTokenAccount(userId)
            .orElseThrow(() -> new IllegalArgumentException("Token account not found for user: " + userId));
        
        account.activate();
        tokenAccountRepository.save(account);
    }
    
    /**
     * 지갑 주소 유효성 검증
     */
    public boolean isValidWalletAddress(String walletAddress) {
        String networkType = getNetworkTypeFromWalletAddress(walletAddress);
        return blockchainClientFactory.getBlockchainClient(networkType).isValidAddress(walletAddress);
    }
    
    /**
     * 토큰 잔액 조회
     */
    public TokenAmount getBalance(String walletAddress) {
        String networkType = getNetworkTypeFromWalletAddress(walletAddress);
        BigDecimal balance = blockchainClientFactory.getBlockchainClient(networkType)
            .getTokenBalance(walletAddress, getTokenContractAddress(walletAddress));
        return new TokenAmount(balance);
    }
    
    /**
     * 스테이킹된 토큰 잔액 조회
     */
    public TokenAmount getStakedBalance(String walletAddress) {
        String networkType = getNetworkTypeFromWalletAddress(walletAddress);
        BigDecimal stakedBalance = blockchainClientFactory.getBlockchainClient(networkType)
            .getStakedTokenBalance(walletAddress, getTokenContractAddress(walletAddress));
        return new TokenAmount(stakedBalance);
    }
    
    /**
     * 지갑 주소에 따른 네트워크 타입 반환
     */
    private String getNetworkTypeFromWalletAddress(String walletAddress) {
        if (walletAddress.startsWith("0x")) {
            return "ETHEREUM";
        } else {
            return "SOLANA";
        }
    }
    
    /**
     * 지갑 주소에 따른 토큰 컨트랙트 주소 반환
     */
    private String getTokenContractAddress(String walletAddress) {
        // 네트워크 타입에 따라 토큰 컨트랙트 주소 반환
        // 실제 구현에서는 설정 파일이나 데이터베이스에서 조회
        if (walletAddress.startsWith("0x")) {
            return "0x1234567890123456789012345678901234567890"; // Ethereum 토큰 컨트랙트 주소
        } else {
            return "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"; // Solana 토큰 컨트랙트 주소
        }
    }
} 