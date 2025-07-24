package com.bloominggrace.governance.wallet.application.dto;

import com.bloominggrace.governance.wallet.domain.service.WalletService;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 지갑 잠금 해제 응답 DTO
 */
@Data
@AllArgsConstructor
public class UnlockWalletResponse {
    
    private final boolean success;
    private final String walletAddress;
    private final String message;
    private final String networkType;
    
    public static UnlockWalletResponse from(WalletService.UnlockResult result, String networkType) {
        return new UnlockWalletResponse(
            result.isSuccess(),
            result.getWalletAddress(),
            result.getMessage(),
            networkType
        );
    }
} 