package com.bloominggrace.governance.token.application.controller;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.application.service.TokenApplicationService;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.shared.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
public class TokenController {
    
    private final TokenApplicationService tokenApplicationService;
    
    /**
     * 토큰 발행 (Mint)
     */
    @PostMapping("/mint")
    public ResponseEntity<String> mintTokens(
            @RequestParam String userId,
            @RequestParam String walletAddress,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam String networkType) {
        
        String transactionSignature = tokenApplicationService.mintTokens(
            new UserId(UUID.fromString(userId)),
            walletAddress,
            amount,
            description,
            networkType
        );
        
        return ResponseEntity.ok(transactionSignature);
    }
    
    /**
     * 토큰 소각 (Burn)
     */
    @PostMapping("/burn")
    public ResponseEntity<String> burnTokens(
            @RequestParam String userId,
            @RequestParam String walletAddress,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam String networkType) {
        
        String transactionSignature = tokenApplicationService.burnTokens(
            new UserId(UUID.fromString(userId)),
            walletAddress,
            amount,
            description,
            networkType
        );
        
        return ResponseEntity.ok(transactionSignature);
    }
    
    /**
     * 토큰 전송
     */
    @PostMapping("/transfer")
    public ResponseEntity<String> transferTokens(
            @RequestParam String fromUserId,
            @RequestParam String fromWalletAddress,
            @RequestParam String toWalletAddress,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam String networkType) {
        
        String transactionSignature = tokenApplicationService.transferTokens(
            new UserId(UUID.fromString(fromUserId)),
            fromWalletAddress,
            toWalletAddress,
            amount,
            description,
            networkType
        );
        
        return ResponseEntity.ok(transactionSignature);
    }
    
    /**
     * 토큰 스테이킹
     */
    @PostMapping("/stake")
    public ResponseEntity<String> stakeTokens(
            @RequestParam String userId,
            @RequestParam String walletAddress,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam String networkType) {
        
        String transactionSignature = tokenApplicationService.stakeTokens(
            new UserId(UUID.fromString(userId)),
            walletAddress,
            amount,
            description,
            networkType
        );
        
        return ResponseEntity.ok(transactionSignature);
    }
    
    /**
     * 토큰 언스테이킹
     */
    @PostMapping("/unstake")
    public ResponseEntity<String> unstakeTokens(
            @RequestParam String userId,
            @RequestParam String walletAddress,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam String networkType) {
        
        String transactionSignature = tokenApplicationService.unstakeTokens(
            new UserId(UUID.fromString(userId)),
            walletAddress,
            amount,
            description,
            networkType
        );
        
        return ResponseEntity.ok(transactionSignature);
    }
    
    /**
     * 투표권 계산
     */
    @GetMapping("/voting-power/{userId}")
    public ResponseEntity<Long> getVotingPower(@PathVariable String userId) {
        long votingPower = tokenApplicationService.calculateVotingPower(new UserId(UUID.fromString(userId)));
        return ResponseEntity.ok(votingPower);
    }
    
    /**
     * 토큰 계정 조회
     */
    @GetMapping("/account/{userId}")
    public ResponseEntity<TokenAccount> getTokenAccount(@PathVariable String userId) {
        Optional<TokenAccount> account = tokenApplicationService.getTokenAccount(new UserId(UUID.fromString(userId)));
        return account.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 지갑 주소로 토큰 계정 조회
     */
    @GetMapping("/account/wallet/{walletAddress}")
    public ResponseEntity<TokenAccount> getTokenAccountByWalletAddress(@PathVariable String walletAddress) {
        List<TokenAccount> accounts = tokenApplicationService.getTokenAccountByWalletAddress(walletAddress);
        Optional<TokenAccount> account = accounts.stream().findFirst();
        return account.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 사용자의 모든 토큰 계정 조회
     */
    @GetMapping("/accounts/{userId}")
    public ResponseEntity<List<TokenAccount>> getAllTokenAccounts(@PathVariable String userId) {
        List<TokenAccount> accounts = tokenApplicationService.getAllTokenAccountsByUserId(new UserId(UUID.fromString(userId)));
        return ResponseEntity.ok(accounts);
    }
    
    /**
     * 트랜잭션 히스토리 조회
     */
    @GetMapping("/transactions/{userId}")
    public ResponseEntity<List<Transaction>> getTransactionHistory(@PathVariable String userId) {
        List<Transaction> transactions = tokenApplicationService.getTransactionHistory(new UserId(UUID.fromString(userId)));
        return ResponseEntity.ok(transactions);
    }
} 