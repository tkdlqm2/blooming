package com.bloominggrace.governance.wallet.application.service;

import com.bloominggrace.governance.wallet.domain.service.BlockchainClient;
import com.bloominggrace.governance.wallet.infrastructure.service.EthereumBlockchainClient;
import com.bloominggrace.governance.wallet.infrastructure.service.SolanaBlockchainClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 블록체인 클라이언트 팩토리
 * 네트워크 타입에 따라 적절한 블록체인 클라이언트를 반환
 */
@Component
@RequiredArgsConstructor
public class BlockchainClientFactory {
    
    private final EthereumBlockchainClient ethereumBlockchainClient;
    private final SolanaBlockchainClient solanaBlockchainClient;
    
    /**
     * 네트워크 타입에 따라 적절한 블록체인 클라이언트를 반환합니다.
     * 
     * @param networkType 네트워크 타입 (ETHEREUM, SOLANA)
     * @return 블록체인 클라이언트
     * @throws IllegalArgumentException 지원하지 않는 네트워크 타입인 경우
     */
    public BlockchainClient getBlockchainClient(String networkType) {
        switch (networkType.toUpperCase()) {
            case "ETHEREUM":
                return ethereumBlockchainClient;
            case "SOLANA":
                return solanaBlockchainClient;
            default:
                throw new IllegalArgumentException("Unsupported network type: " + networkType);
        }
    }
    
    /**
     * 이더리움 블록체인 클라이언트를 반환합니다.
     * 
     * @return 이더리움 블록체인 클라이언트
     */
    public EthereumBlockchainClient getEthereumBlockchainClient() {
        return ethereumBlockchainClient;
    }
    
    /**
     * 솔라나 블록체인 클라이언트를 반환합니다.
     * 
     * @return 솔라나 블록체인 클라이언트
     */
    public SolanaBlockchainClient getSolanaBlockchainClient() {
        return solanaBlockchainClient;
    }
} 