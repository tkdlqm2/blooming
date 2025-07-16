package com.bloominggrace.governance.wallet.application.service;

import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.service.WalletService;
import com.bloominggrace.governance.wallet.infrastructure.repository.WalletRepository;
import com.bloominggrace.governance.wallet.infrastructure.service.ethereum.EthereumWalletService;
import com.bloominggrace.governance.wallet.infrastructure.service.solana.SolanaWalletService;
import com.bloominggrace.governance.shared.domain.service.EncryptionService;
import com.bloominggrace.governance.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 지갑 서비스 팩토리
 * 네트워크 타입에 따라 적절한 지갑 서비스를 반환
 */
@RequiredArgsConstructor
@Component
public class WalletServiceFactory {
    private final WalletRepository walletRepository;
    private final EncryptionService encryptionService;
    private final UserRepository userRepository;

    /**
     * 네트워크 타입에 따라 적절한 지갑 서비스를 반환합니다.
     * 
     * @param networkType 네트워크 타입 (ETHEREUM, SOLANA)
     * @return 지갑 서비스
     * @throws IllegalArgumentException 지원하지 않는 네트워크 타입인 경우
     */
    public WalletService getWalletService(NetworkType networkType) {
        switch (networkType) {
            case ETHEREUM:
                return new EthereumWalletService(walletRepository, encryptionService, userRepository);
            case SOLANA:
                return new SolanaWalletService(walletRepository, encryptionService, userRepository);
            default:
                throw new IllegalArgumentException("Unsupported network type: " + networkType);
        }
    }
} 