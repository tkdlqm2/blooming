package com.bloominggrace.governance.point.application.controller;

import com.bloominggrace.governance.point.application.service.PointManagementService;
import com.bloominggrace.governance.point.domain.model.PointAmount;
import com.bloominggrace.governance.point.domain.model.PointTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointManagementService pointManagementService;

    @PostMapping("/earn")
    public ResponseEntity<Void> earnPoints(@RequestBody EarnPointsRequest request) {
        PointAmount amount = PointAmount.of(request.getAmount());
        pointManagementService.earnPoints(request.getUserId(), amount, request.getReason());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/balance/{userId}")
    public ResponseEntity<PointBalanceResponse> getPointBalance(@PathVariable UUID userId) {
        PointManagementService.PointBalance balance = pointManagementService.getPointBalance(userId);
        return ResponseEntity.ok(new PointBalanceResponse(
            balance.getAvailableBalance().getAmount(),
            balance.getFrozenBalance().getAmount(),
            balance.getTotalBalance().getAmount()
        ));
    }

    @GetMapping("/transactions/{userId}")
    public ResponseEntity<List<PointTransaction>> getPointTransactions(@PathVariable UUID userId) {
        List<PointTransaction> transactions = pointManagementService.getPointTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    public static class EarnPointsRequest {
        private UUID userId;
        private BigDecimal amount;
        private String reason;

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class PointBalanceResponse {
        private final BigDecimal availableBalance;
        private final BigDecimal frozenBalance;
        private final BigDecimal totalBalance;

        public PointBalanceResponse(BigDecimal availableBalance, BigDecimal frozenBalance, BigDecimal totalBalance) {
            this.availableBalance = availableBalance;
            this.frozenBalance = frozenBalance;
            this.totalBalance = totalBalance;
        }

        public BigDecimal getAvailableBalance() { return availableBalance; }
        public BigDecimal getFrozenBalance() { return frozenBalance; }
        public BigDecimal getTotalBalance() { return totalBalance; }
    }
} 