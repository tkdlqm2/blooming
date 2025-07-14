package com.bloominggrace.governance.token.infrastructure.service;

import com.bloominggrace.governance.token.domain.model.TokenAmount;
import com.bloominggrace.governance.token.domain.service.SolanaTokenService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MockSolanaTokenService implements SolanaTokenService {
    
    @Override
    public String mintTokens(String walletAddress, TokenAmount amount) {
        // 실제 Solana RPC API 호출 대신 Mock 구현
        if (!isValidWalletAddress(walletAddress)) {
            throw new IllegalArgumentException("Invalid wallet address: " + walletAddress);
        }
        
        // Mock 트랜잭션 서명 생성
        String transactionSignature = "5J7X" + UUID.randomUUID().toString().replace("-", "").substring(0, 60);
        
        // 실제 구현에서는 Solana RPC API를 호출하여 토큰 민팅
        // return solanaRpcClient.mintTokens(walletAddress, amount.getAmount());
        
        return transactionSignature;
    }
    
    @Override
    public String stakeTokens(String walletAddress, TokenAmount amount) {
        if (!isValidWalletAddress(walletAddress)) {
            throw new IllegalArgumentException("Invalid wallet address: " + walletAddress);
        }
        
        // Mock 트랜잭션 서명 생성
        String transactionSignature = "5J7X" + UUID.randomUUID().toString().replace("-", "").substring(0, 60);
        
        return transactionSignature;
    }
    
    @Override
    public String unstakeTokens(String walletAddress, TokenAmount amount) {
        if (!isValidWalletAddress(walletAddress)) {
            throw new IllegalArgumentException("Invalid wallet address: " + walletAddress);
        }
        
        // Mock 트랜잭션 서명 생성
        String transactionSignature = "5J7X" + UUID.randomUUID().toString().replace("-", "").substring(0, 60);
        
        return transactionSignature;
    }
    
    @Override
    public TokenAmount getBalance(String walletAddress) {
        if (!isValidWalletAddress(walletAddress)) {
            throw new IllegalArgumentException("Invalid wallet address: " + walletAddress);
        }
        
        // Mock 잔액 반환 (실제로는 Solana RPC API 호출)
        return new TokenAmount("100.0");
    }
    
    @Override
    public boolean isValidWalletAddress(String walletAddress) {
        // Solana 주소는 32-44자 길이의 Base58 인코딩된 문자열
        return walletAddress != null && 
               walletAddress.length() >= 32 && 
               walletAddress.length() <= 44 &&
               walletAddress.matches("^[1-9A-HJ-NP-Za-km-z]+$");
    }
} 