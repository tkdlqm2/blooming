package com.bloominggrace.governance.wallet.application.dto;

import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 지갑 DTO
 */
public class WalletDto {
    
    private UUID id;
    private UUID userId;
    private String walletAddress;
    private NetworkType networkType;
    private boolean active;
    private BigDecimal balance;
    
    public WalletDto() {}
    
    public WalletDto(UUID id, UUID userId, String walletAddress, NetworkType networkType, boolean active) {
        this.id = id;
        this.userId = userId;
        this.walletAddress = walletAddress;
        this.networkType = networkType;
        this.active = active;
    }
    
    public WalletDto(UUID id, UUID userId, String walletAddress, NetworkType networkType, boolean active, BigDecimal balance) {
        this.id = id;
        this.userId = userId;
        this.walletAddress = walletAddress;
        this.networkType = networkType;
        this.active = active;
        this.balance = balance;
    }
    
    public static WalletDto from(Wallet wallet) {
        return WalletDto.builder()
            .id(wallet.getId())
            .userId(wallet.getUser() != null ? wallet.getUser().getId() : null)
            .walletAddress(wallet.getWalletAddress())
            .networkType(wallet.getNetworkType())
            .active(wallet.isActive())
            .build();
    }
    
    public static WalletDto from(Wallet wallet, BigDecimal balance) {
        return WalletDto.builder()
            .id(wallet.getId())
            .userId(wallet.getUser() != null ? wallet.getUser().getId() : null)
            .walletAddress(wallet.getWalletAddress())
            .networkType(wallet.getNetworkType())
            .active(wallet.isActive())
            .balance(balance)
            .build();
    }
    
    public static WalletDtoBuilder builder() {
        return new WalletDtoBuilder();
    }
    
    public static class WalletDtoBuilder {
        private UUID id;
        private UUID userId;
        private String walletAddress;
        private NetworkType networkType;
        private boolean active;
        private BigDecimal balance;
        
        public WalletDtoBuilder id(UUID id) {
            this.id = id;
            return this;
        }
        
        public WalletDtoBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }
        
        public WalletDtoBuilder walletAddress(String walletAddress) {
            this.walletAddress = walletAddress;
            return this;
        }
        
        public WalletDtoBuilder networkType(NetworkType networkType) {
            this.networkType = networkType;
            return this;
        }
        
        public WalletDtoBuilder active(boolean active) {
            this.active = active;
            return this;
        }
        
        public WalletDtoBuilder balance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }
        
        public WalletDto build() {
            return new WalletDto(id, userId, walletAddress, networkType, active, balance);
        }
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public String getWalletAddress() {
        return walletAddress;
    }
    
    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }
    
    public NetworkType getNetworkType() {
        return networkType;
    }
    
    public void setNetworkType(NetworkType networkType) {
        this.networkType = networkType;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
} 