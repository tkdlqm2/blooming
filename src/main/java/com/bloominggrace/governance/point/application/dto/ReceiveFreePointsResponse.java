package com.bloominggrace.governance.point.application.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ReceiveFreePointsResponse {
    private final UUID userId;
    private final BigDecimal receivedAmount;
    private final BigDecimal newBalance;
    private final LocalDateTime receivedAt;
    private final String message;

    public ReceiveFreePointsResponse(UUID userId, BigDecimal receivedAmount, BigDecimal newBalance) {
        this.userId = userId;
        this.receivedAmount = receivedAmount;
        this.newBalance = newBalance;
        this.receivedAt = LocalDateTime.now();
        this.message = "무료 포인트가 성공적으로 수령되었습니다.";
    }
} 