package com.bloominggrace.governance.token.application.dto;

import com.bloominggrace.governance.wallet.domain.model.NetworkType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 토큰 계정 정보를 전달하는 DTO
 */
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

    public TokenAccountDto() {}

    public TokenAccountDto(UUID id, UUID walletId, String userId, BigDecimal totalBalance, 
                          BigDecimal stakedBalance, BigDecimal availableBalance, NetworkType network,
                          String contract, String symbol, boolean isActive, 
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.walletId = walletId;
        this.userId = userId;
        this.totalBalance = totalBalance;
        this.stakedBalance = stakedBalance;
        this.availableBalance = availableBalance;
        this.network = network;
        this.contract = contract;
        this.symbol = symbol;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public void setWalletId(UUID walletId) {
        this.walletId = walletId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public BigDecimal getStakedBalance() {
        return stakedBalance;
    }

    public void setStakedBalance(BigDecimal stakedBalance) {
        this.stakedBalance = stakedBalance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public NetworkType getNetwork() {
        return network;
    }

    public void setNetwork(NetworkType network) {
        this.network = network;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 