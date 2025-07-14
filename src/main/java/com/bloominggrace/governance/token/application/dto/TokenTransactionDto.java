package com.bloominggrace.governance.token.application.dto;

import com.bloominggrace.governance.token.domain.model.TokenAmount;
import com.bloominggrace.governance.token.domain.model.TokenTransaction;
import com.bloominggrace.governance.token.domain.model.TokenTransactionStatus;
import com.bloominggrace.governance.token.domain.model.TokenTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TokenTransactionDto {
    private final UUID id;
    private final UUID userId;
    private final String walletAddress;
    private final String transactionType;
    private final BigDecimal amount;
    private final String transactionSignature;
    private final String description;
    private final LocalDateTime createdAt;
    private final String status;

    public TokenTransactionDto(
            UUID id,
            UUID userId,
            String walletAddress,
            String transactionType,
            BigDecimal amount,
            String transactionSignature,
            String description,
            LocalDateTime createdAt,
            String status) {
        this.id = id;
        this.userId = userId;
        this.walletAddress = walletAddress;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionSignature = transactionSignature;
        this.description = description;
        this.createdAt = createdAt;
        this.status = status;
    }

    public static TokenTransactionDto from(TokenTransaction transaction) {
        return new TokenTransactionDto(
            transaction.getId().getValue(),
            transaction.getUserId().getValue(),
            transaction.getWalletAddress(),
            transaction.getTransactionType().name(),
            transaction.getAmount().getAmount(),
            transaction.getTransactionSignature(),
            transaction.getDescription(),
            transaction.getCreatedAt(),
            transaction.getStatus().name()
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

    public String getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTransactionSignature() {
        return transactionSignature;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }
} 