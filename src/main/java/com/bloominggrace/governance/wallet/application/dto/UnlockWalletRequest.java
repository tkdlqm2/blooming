package com.bloominggrace.governance.wallet.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 지갑 잠금 해제 요청 DTO
 */
@Data
public class UnlockWalletRequest {
    
    @NotNull(message = "Wallet address is required")
    private String walletAddress;
    
    @NotNull(message = "Network type is required")
    @Pattern(regexp = "^(ETHEREUM|SOLANA)$", message = "Network type must be ETHEREUM or SOLANA")
    private String networkType;
} 