package com.bloominggrace.governance.token.application.dto;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.token.domain.model.TokenBalance;

import java.time.LocalDateTime;
import java.util.UUID;

public class TokenAccountDto {
    private final UUID id;
    private final UUID userId;
    private final String walletAddress;
    private final TokenBalanceDto balance;
    private final boolean isActive;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public TokenAccountDto(
            UUID id,
            UUID userId,
            String walletAddress,
            TokenBalanceDto balance,
            boolean isActive,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.walletAddress = walletAddress;
        this.balance = balance;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TokenAccountDto from(TokenAccount tokenAccount) {
        return new TokenAccountDto(
            tokenAccount.getId().getValue(),
            tokenAccount.getUserId().getValue(),
            tokenAccount.getWalletAddress(),
            TokenBalanceDto.from(tokenAccount.getBalance()),
            tokenAccount.isActive(),
            tokenAccount.getCreatedAt(),
            tokenAccount.getUpdatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public TokenBalanceDto getBalance() {
        return balance;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
} 