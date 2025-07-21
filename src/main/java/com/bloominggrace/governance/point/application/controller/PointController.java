package com.bloominggrace.governance.point.application.controller;

import com.bloominggrace.governance.point.application.dto.ReceiveFreePointsRequest;
import com.bloominggrace.governance.point.application.dto.ReceiveFreePointsResponse;
import com.bloominggrace.governance.point.application.service.PointManagementService;
import com.bloominggrace.governance.point.domain.model.PointAmount;
import com.bloominggrace.governance.point.domain.model.PointTransaction;
import com.bloominggrace.governance.shared.infrastructure.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import com.bloominggrace.governance.point.domain.model.PointAccount;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
@Slf4j
public class PointController {

    private final PointManagementService pointManagementService;
    private final JwtService jwtService;

    // 무료 포인트 수령 (포인트 적립으로 통합)
    @PostMapping("/receive-free")
    public ResponseEntity<ReceiveFreePointsResponse> receiveFreePoints(
            @RequestBody ReceiveFreePointsRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // "Bearer " 접두사 제거
            String actualToken = token.replace("Bearer ", "");
            UUID userId = jwtService.getUserIdFromToken(actualToken);
            log.info("무료 포인트 수령 요청: userId={}, amount={}", userId, request.getAmount());
            
            PointAmount amount = PointAmount.of(request.getAmount());
            // earnPoints 메서드 사용하여 "무료 포인트 수령" 이유로 적립
            pointManagementService.earnPoints(userId, amount, "무료 포인트 수령");
            
            // 잔액 조회
            PointManagementService.PointBalance balance = pointManagementService.getPointBalance(userId);
            
            ReceiveFreePointsResponse response = new ReceiveFreePointsResponse(
                userId,
                request.getAmount(),
                balance.getTotalBalance().getAmount()
            );
            
            log.info("무료 포인트 수령 성공: userId={}, receivedAmount={}, newBalance={}", 
                userId, request.getAmount(), balance.getTotalBalance().getAmount());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("무료 포인트 수령 실패: amount={}, error={}", 
                request.getAmount(), e.getMessage(), e);
            throw e;
        }
    }

    // 포인트 적립
    @PostMapping("/earn")
    public ResponseEntity<String> earnPoints(
            @RequestBody EarnPointsRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // "Bearer " 접두사 제거
            String actualToken = token.replace("Bearer ", "");
            UUID userId = jwtService.getUserIdFromToken(actualToken);
            log.info("포인트 적립 요청: userId={}, amount={}, reason={}", 
                userId, request.getAmount(), request.getReason());
            
            PointAmount amount = PointAmount.of(request.getAmount());
            pointManagementService.earnPoints(userId, amount, request.getReason());
            
            log.info("포인트 적립 성공: userId={}, amount={}", userId, request.getAmount());
            return ResponseEntity.ok("포인트가 성공적으로 적립되었습니다.");
        } catch (Exception e) {
            log.error("포인트 적립 실패: amount={}, error={}", 
                request.getAmount(), e.getMessage(), e);
            throw e;
        }
    }

    // 포인트 잔액 조회
    @GetMapping("/balance")
    public ResponseEntity<PointBalanceResponse> getPointBalance(@RequestHeader("Authorization") String token) {
        try {
            // "Bearer " 접두사 제거
            String actualToken = token.replace("Bearer ", "");
            UUID userId = jwtService.getUserIdFromToken(actualToken);
            log.info("포인트 잔액 조회 요청: userId={}", userId);
            
            PointManagementService.PointBalance balance = pointManagementService.getPointBalance(userId);
            
            PointBalanceResponse response = new PointBalanceResponse(
                balance.getAvailableBalance().getAmount(),
                balance.getFrozenBalance().getAmount(),
                balance.getTotalBalance().getAmount()
            );
            
            log.info("포인트 잔액 조회 성공: userId={}, availableBalance={}, frozenBalance={}, totalBalance={}", 
                userId, balance.getAvailableBalance().getAmount(), 
                balance.getFrozenBalance().getAmount(), balance.getTotalBalance().getAmount());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("포인트 잔액 조회 실패: error={}", e.getMessage(), e);
            throw e;
        }
    }

    // 포인트 거래 내역 조회
    @GetMapping("/transactions")
    public ResponseEntity<List<PointTransaction>> getPointTransactions(@RequestHeader("Authorization") String token) {
        try {
            // "Bearer " 접두사 제거
            String actualToken = token.replace("Bearer ", "");
            UUID userId = jwtService.getUserIdFromToken(actualToken);
            log.info("포인트 거래 내역 조회 요청: userId={}", userId);
            
            List<PointTransaction> transactions = pointManagementService.getPointTransactions(userId);
            
            log.info("포인트 거래 내역 조회 성공: userId={}, transactionCount={}", userId, transactions.size());
            
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("포인트 거래 내역 조회 실패: error={}", e.getMessage(), e);
            throw e;
        }
    }

    // 포인트 계정 생성 (테스트용)
    @PostMapping("/create-account")
    public ResponseEntity<String> createPointAccount(@RequestHeader("Authorization") String token) {
        try {
            // "Bearer " 접두사 제거
            String actualToken = token.replace("Bearer ", "");
            UUID userId = jwtService.getUserIdFromToken(actualToken);
            log.info("포인트 계정 생성 요청: userId={}", userId);
            
            // getOrCreatePointAccount 메서드를 직접 호출
            PointAccount account = pointManagementService.getOrCreatePointAccount(userId);
            
            log.info("포인트 계정 생성 성공: userId={}, accountId={}", userId, account.getId());
            
            return ResponseEntity.ok("포인트 계정이 생성되었습니다. 계정 ID: " + account.getId());
        } catch (Exception e) {
            log.error("포인트 계정 생성 실패: error={}", e.getMessage(), e);
            throw e;
        }
    }

    // DTO 클래스들
    public static class EarnPointsRequest {
        private BigDecimal amount;
        private String reason;

        // Getters and Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class PointBalanceResponse {
        private BigDecimal availableBalance;
        private BigDecimal frozenBalance;
        private BigDecimal totalBalance;

        public PointBalanceResponse(BigDecimal availableBalance, BigDecimal frozenBalance, BigDecimal totalBalance) {
            this.availableBalance = availableBalance;
            this.frozenBalance = frozenBalance;
            this.totalBalance = totalBalance;
        }

        // Getters
        public BigDecimal getAvailableBalance() { return availableBalance; }
        public BigDecimal getFrozenBalance() { return frozenBalance; }
        public BigDecimal getTotalBalance() { return totalBalance; }
    }
} 