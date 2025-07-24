package com.bloominggrace.governance.token.application.dto;

import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 토큰 계정 정보를 전달하는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenAccountDto {
    private UUID id;
    private UUID walletId;
    private String userId;
    private BigDecimal totalBalance;
    private BigDecimal stakedBalance;
    private BigDecimal availableBalance;
    private NetworkType network;
    private String contract;
    private String symbol;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 