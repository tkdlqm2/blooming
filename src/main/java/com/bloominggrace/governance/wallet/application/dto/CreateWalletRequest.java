package com.bloominggrace.governance.wallet.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 지갑 생성 요청 DTO
 */
@Data
public class CreateWalletRequest {
    
    @NotNull(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Network type is required")
    @Pattern(regexp = "^(ETHEREUM|SOLANA)$", message = "Network type must be ETHEREUM or SOLANA")
    private String networkType;
} 