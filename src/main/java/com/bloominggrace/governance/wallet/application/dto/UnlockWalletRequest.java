package com.bloominggrace.governance.wallet.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 지갑 잠금 해제 요청 DTO
 */
public class UnlockWalletRequest {
    
    @NotNull(message = "Wallet address is required")
    private String walletAddress;
    
    @NotNull(message = "Network type is required")
    @Pattern(regexp = "^(ETHEREUM|SOLANA)$", message = "Network type must be ETHEREUM or SOLANA")
    private String networkType;
    
    public UnlockWalletRequest() {}
    
    public UnlockWalletRequest(String walletAddress, String networkType) {
        this.walletAddress = walletAddress;
        this.networkType = networkType;
    }
    
    // Getters and Setters
    public String getWalletAddress() {
        return walletAddress;
    }
    
    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }
    
    public String getNetworkType() {
        return networkType;
    }
    
    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }
} 