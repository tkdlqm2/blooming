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