package com.bloominggrace.governance.blockchain.application.service;

import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 네트워크 타입에 따라 적절한 BlockchainClient를 제공하는 팩토리 서비스
 */
@Service
@RequiredArgsConstructor
public class BlockchainClientFactory {
    
    private final BlockchainClient ethereumBlockchainClient;
    private final BlockchainClient solanaBlockchainClient;
    
    /**
     * 네트워크 타입에 따라 적절한 BlockchainClient를 반환합니다.
     * 
     * @param networkType 네트워크 타입
     * @return 해당 네트워크의 BlockchainClient
     * @throws IllegalArgumentException 지원하지 않는 네트워크 타입인 경우
     */
    public BlockchainClient getClient(NetworkType networkType) {
        switch (networkType) {
            case ETHEREUM:
                return ethereumBlockchainClient;
            case SOLANA:
                return solanaBlockchainClient;
            default:
                throw new IllegalArgumentException("Unsupported network type: " + networkType);
        }
    }
    
    /**
     * 네트워크 타입 문자열에 따라 적절한 BlockchainClient를 반환합니다.
     * 
     * @param networkType 네트워크 타입 문자열
     * @return 해당 네트워크의 BlockchainClient
     * @throws IllegalArgumentException 지원하지 않는 네트워크 타입인 경우
     */
    public BlockchainClient getClient(String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            return getClient(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported network type: " + networkType);
        }
    }
} 