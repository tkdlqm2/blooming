package com.bloominggrace.governance.wallet.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 지갑 생성 요청 DTO
 */
public class CreateWalletRequest {
    
    @NotNull(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Network type is required")
    @Pattern(regexp = "^(ETHEREUM|SOLANA)$", message = "Network type must be ETHEREUM or SOLANA")
    private String networkType;
    
    public CreateWalletRequest() {}
    
    public CreateWalletRequest(String userId, String networkType) {
        this.userId = userId;
        this.networkType = networkType;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getNetworkType() {
        return networkType;
    }
    
    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }
} 