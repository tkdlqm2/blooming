package com.bloominggrace.governance.wallet.application.service;

import com.bloominggrace.governance.wallet.domain.service.WalletService;
import com.bloominggrace.governance.wallet.infrastructure.service.EthereumWalletService;
import com.bloominggrace.governance.wallet.infrastructure.service.SolanaWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 지갑 서비스 팩토리
 * 네트워크 타입에 따라 적절한 지갑 서비스를 반환
 */
@Component
@RequiredArgsConstructor
public class WalletServiceFactory {
    
    private final EthereumWalletService ethereumWalletService;
    private final SolanaWalletService solanaWalletService;
    
    /**
     * 네트워크 타입에 따라 적절한 지갑 서비스를 반환합니다.
     * 
     * @param networkType 네트워크 타입 (ETHEREUM, SOLANA)
     * @return 지갑 서비스
     * @throws IllegalArgumentException 지원하지 않는 네트워크 타입인 경우
     */
    public WalletService getWalletService(String networkType) {
        switch (networkType.toUpperCase()) {
            case "ETHEREUM":
                return ethereumWalletService;
            case "SOLANA":
                return solanaWalletService;
            default:
                throw new IllegalArgumentException("Unsupported network type: " + networkType);
        }
    }
    
    /**
     * 이더리움 지갑 서비스를 반환합니다.
     * 
     * @return 이더리움 지갑 서비스
     */
    public EthereumWalletService getEthereumWalletService() {
        return ethereumWalletService;
    }
    
    /**
     * 솔라나 지갑 서비스를 반환합니다.
     * 
     * @return 솔라나 지갑 서비스
     */
    public SolanaWalletService getSolanaWalletService() {
        return solanaWalletService;
    }
} 