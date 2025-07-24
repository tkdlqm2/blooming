package com.bloominggrace.governance.token.infrastructure.controller;

import com.bloominggrace.governance.token.application.dto.CreateTokenAccountRequest;
import com.bloominggrace.governance.token.application.dto.TokenAccountDto;
import com.bloominggrace.governance.token.application.service.TokenAccountApplicationService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 토큰 계정 관리를 위한 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/token-accounts")
public class TokenAccountController {
    
    private final TokenAccountApplicationService tokenAccountApplicationService;
    
    public TokenAccountController(TokenAccountApplicationService tokenAccountApplicationService) {
        this.tokenAccountApplicationService = tokenAccountApplicationService;
    }
    
    /**
     * 새로운 토큰 계정을 생성합니다.
     * 
     * @param request 토큰 계정 생성 요청
     * @return 생성된 토큰 계정 정보
     */
    @PostMapping
    public ResponseEntity<TokenAccountDto> createTokenAccount(@RequestBody CreateTokenAccountRequest request) {
        try {
            TokenAccountDto tokenAccount = tokenAccountApplicationService.createTokenAccount(request);
            return ResponseEntity.ok(tokenAccount);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 토큰 계정 ID로 조회합니다.
     * 
     * @param id 토큰 계정 ID
     * @return 토큰 계정 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<TokenAccountDto> getTokenAccount(@PathVariable UUID id) {
        try {
            TokenAccountDto tokenAccount = tokenAccountApplicationService.findById(id);
            return ResponseEntity.ok(tokenAccount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 사용자 ID로 모든 토큰 계정을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 토큰 계정 목록
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TokenAccountDto>> getTokenAccountsByUserId(@PathVariable String userId) {
        try {
            List<TokenAccountDto> tokenAccounts = tokenAccountApplicationService.findByUserId(userId);
            return ResponseEntity.ok(tokenAccounts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 지갑 주소로 모든 토큰 계정을 조회합니다.
     * 
     * @param walletAddress 지갑 주소
     * @return 토큰 계정 목록
     */
    @GetMapping("/wallet/{walletAddress}")
    public ResponseEntity<List<TokenAccountDto>> getTokenAccountsByWalletAddress(@PathVariable String walletAddress) {
        try {
            List<TokenAccountDto> tokenAccounts = tokenAccountApplicationService.findByWalletAddress(walletAddress);
            return ResponseEntity.ok(tokenAccounts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 지갑 주소와 네트워크로 토큰 계정을 조회합니다.
     * 
     * @param walletAddress 지갑 주소
     * @param network 네트워크 타입
     * @return 토큰 계정 목록
     */
    @GetMapping("/wallet/{walletAddress}/network/{network}")
    public ResponseEntity<List<TokenAccountDto>> getTokenAccountsByWalletAddressAndNetwork(
            @PathVariable String walletAddress, 
            @PathVariable NetworkType network) {
        try {
            List<TokenAccountDto> tokenAccounts = tokenAccountApplicationService.findByWalletAddressAndNetwork(walletAddress, network);
            return ResponseEntity.ok(tokenAccounts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 모든 토큰 계정을 조회합니다.
     * 
     * @return 토큰 계정 목록
     */
    @GetMapping
    public ResponseEntity<List<TokenAccountDto>> getAllTokenAccounts() {
        try {
            List<TokenAccountDto> tokenAccounts = tokenAccountApplicationService.findAll();
            return ResponseEntity.ok(tokenAccounts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 토큰 계정을 삭제합니다.
     * 
     * @param id 토큰 계정 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTokenAccount(@PathVariable UUID id) {
        try {
            tokenAccountApplicationService.deleteTokenAccount(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 