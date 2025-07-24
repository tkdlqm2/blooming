package com.bloominggrace.governance.wallet.application.dto;

import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 지갑 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDto {
    
    private UUID id;
    private UUID userId;
    private String walletAddress;
    private NetworkType networkType;
    private boolean active;
    private BigDecimal balance;
    
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
} 