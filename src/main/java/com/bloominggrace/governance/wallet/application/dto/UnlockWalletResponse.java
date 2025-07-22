package com.bloominggrace.governance.wallet.application.dto;

/**
 * 지갑 잠금 해제 응답 DTO
 */
public class UnlockWalletResponse {
    
    private final boolean success;
    private final String walletAddress;
    private final String message;
    private final String derivedAddress;
    private final boolean addressMatch;
    private final String networkType;
    
    public UnlockWalletResponse(boolean success, String walletAddress, String message, 
                               String derivedAddress, boolean addressMatch, String networkType) {
        this.success = success;
        this.walletAddress = walletAddress;
        this.message = message;
        this.derivedAddress = derivedAddress;
        this.addressMatch = addressMatch;
        this.networkType = networkType;
    }
    
    public static UnlockWalletResponse from(com.bloominggrace.governance.wallet.domain.service.WalletService.UnlockResult result, String networkType) {
        return new UnlockWalletResponse(
            result.isSuccess(),
            result.getWalletAddress(),
            result.getMessage(),
            result.getDerivedAddress(),
            result.isAddressMatch(),
            networkType
        );
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public String getWalletAddress() {
        return walletAddress;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getDerivedAddress() {
        return derivedAddress;
    }
    
    public boolean isAddressMatch() {
        return addressMatch;
    }
    
    public String getNetworkType() {
        return networkType;
    }
} 