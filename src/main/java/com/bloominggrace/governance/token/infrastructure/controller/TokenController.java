package com.bloominggrace.governance.token.infrastructure.controller;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.application.dto.TokenAccountDto;
import com.bloominggrace.governance.token.application.dto.TokenTransactionDto;
import com.bloominggrace.governance.token.application.service.TokenApplicationService;
import com.bloominggrace.governance.token.domain.model.TokenAmount;
import com.bloominggrace.governance.token.domain.model.TokenTransactionType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {
    
    private final TokenApplicationService tokenApplicationService;
    
    public TokenController(TokenApplicationService tokenApplicationService) {
        this.tokenApplicationService = tokenApplicationService;
    }
    
    @PostMapping("/accounts")
    public ResponseEntity<TokenAccountDto> createTokenAccount(
            @RequestParam UUID userId,
            @RequestParam String walletAddress) {
        
        UserId userIdObj = new UserId(userId);
        var tokenAccount = tokenApplicationService.createTokenAccount(userIdObj, walletAddress, null);
        return ResponseEntity.ok(TokenAccountDto.from(tokenAccount));
    }
    
    @GetMapping("/accounts/{userId}")
    public ResponseEntity<TokenAccountDto> getTokenAccount(@PathVariable UUID userId) {
        UserId userIdObj = new UserId(userId);
        var tokenAccount = tokenApplicationService.getTokenAccount(userIdObj)
            .orElseThrow(() -> new IllegalArgumentException("Token account not found"));
        
        return ResponseEntity.ok(TokenAccountDto.from(tokenAccount));
    }
    
    @GetMapping("/accounts/wallet/{walletAddress}")
    public ResponseEntity<TokenAccountDto> getTokenAccountByWalletAddress(@PathVariable String walletAddress) {
        var tokenAccount = tokenApplicationService.getTokenAccountByWalletAddress(walletAddress)
            .orElseThrow(() -> new IllegalArgumentException("Token account not found"));
        
        return ResponseEntity.ok(TokenAccountDto.from(tokenAccount));
    }
    
    @PostMapping("/mint")
    public ResponseEntity<String> mintTokens(
            @RequestParam UUID userId,
            @RequestParam BigDecimal amount,
            @RequestParam String description) {
        
        UserId userIdObj = new UserId(userId);
        TokenAmount tokenAmount = new TokenAmount(amount);
        String transactionSignature = tokenApplicationService.mintTokens(userIdObj, tokenAmount, description);
        
        return ResponseEntity.ok(transactionSignature);
    }
    
    @PostMapping("/stake")
    public ResponseEntity<String> stakeTokens(
            @RequestParam UUID userId,
            @RequestParam BigDecimal amount) {
        
        UserId userIdObj = new UserId(userId);
        TokenAmount tokenAmount = new TokenAmount(amount);
        String transactionSignature = tokenApplicationService.stakeTokens(userIdObj, tokenAmount);
        
        return ResponseEntity.ok(transactionSignature);
    }
    
    @PostMapping("/unstake")
    public ResponseEntity<String> unstakeTokens(
            @RequestParam UUID userId,
            @RequestParam BigDecimal amount) {
        
        UserId userIdObj = new UserId(userId);
        TokenAmount tokenAmount = new TokenAmount(amount);
        String transactionSignature = tokenApplicationService.unstakeTokens(userIdObj, tokenAmount);
        
        return ResponseEntity.ok(transactionSignature);
    }
    
    @PostMapping("/transfer")
    public ResponseEntity<String> transferTokens(
            @RequestParam UUID fromUserId,
            @RequestParam String toWalletAddress,
            @RequestParam BigDecimal amount,
            @RequestParam String description) {
        
        UserId fromUserIdObj = new UserId(fromUserId);
        TokenAmount tokenAmount = new TokenAmount(amount);
        String transactionSignature = tokenApplicationService.transferTokens(fromUserIdObj, toWalletAddress, tokenAmount, description);
        
        return ResponseEntity.ok(transactionSignature);
    }
    
    @PostMapping("/burn")
    public ResponseEntity<String> burnTokens(
            @RequestParam UUID userId,
            @RequestParam BigDecimal amount,
            @RequestParam String description) {
        
        UserId userIdObj = new UserId(userId);
        TokenAmount tokenAmount = new TokenAmount(amount);
        String transactionSignature = tokenApplicationService.burnTokens(userIdObj, tokenAmount, description);
        
        return ResponseEntity.ok(transactionSignature);
    }
    
    @GetMapping("/transactions/{userId}")
    public ResponseEntity<List<TokenTransactionDto>> getTransactionHistory(@PathVariable UUID userId) {
        UserId userIdObj = new UserId(userId);
        var transactions = tokenApplicationService.getTransactionHistory(userIdObj);
        
        List<TokenTransactionDto> transactionDtos = transactions.stream()
            .map(TokenTransactionDto::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(transactionDtos);
    }
    
    @GetMapping("/transactions/{userId}/{transactionType}")
    public ResponseEntity<List<TokenTransactionDto>> getTransactionHistoryByType(
            @PathVariable UUID userId,
            @PathVariable String transactionType) {
        
        UserId userIdObj = new UserId(userId);
        TokenTransactionType type = TokenTransactionType.valueOf(transactionType.toUpperCase());
        var transactions = tokenApplicationService.getTransactionHistoryByType(userIdObj, type);
        
        List<TokenTransactionDto> transactionDtos = transactions.stream()
            .map(TokenTransactionDto::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(transactionDtos);
    }
    
    @PostMapping("/accounts/{userId}/deactivate")
    public ResponseEntity<Void> deactivateTokenAccount(@PathVariable UUID userId) {
        UserId userIdObj = new UserId(userId);
        tokenApplicationService.deactivateTokenAccount(userIdObj);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/accounts/{userId}/activate")
    public ResponseEntity<Void> activateTokenAccount(@PathVariable UUID userId) {
        UserId userIdObj = new UserId(userId);
        tokenApplicationService.activateTokenAccount(userIdObj);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/balance/{walletAddress}")
    public ResponseEntity<TokenAmount> getBalance(@PathVariable String walletAddress) {
        TokenAmount balance = tokenApplicationService.getBalance(walletAddress);
        return ResponseEntity.ok(balance);
    }
    
    @GetMapping("/staked-balance/{walletAddress}")
    public ResponseEntity<TokenAmount> getStakedBalance(@PathVariable String walletAddress) {
        TokenAmount stakedBalance = tokenApplicationService.getStakedBalance(walletAddress);
        return ResponseEntity.ok(stakedBalance);
    }
    
    @GetMapping("/validate-address/{walletAddress}")
    public ResponseEntity<Boolean> validateWalletAddress(@PathVariable String walletAddress) {
        boolean isValid = tokenApplicationService.isValidWalletAddress(walletAddress);
        return ResponseEntity.ok(isValid);
    }
} 