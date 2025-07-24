package com.bloominggrace.governance.blockchain.application.service;

import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 네트워크 타입에 따라 적절한 BlockchainClient를 제공하는 팩토리 서비스
 */
@Service
public class BlockchainClientFactory {
    
    private final Map<NetworkType, BlockchainClient> blockchainClients;
    
    /**
     * 생성자를 통해 주입받은 BlockchainClient 리스트를 Map으로 변환하여 초기화합니다.
     * 
     * @param clients BlockchainClient 구현체들의 리스트
     */
    public BlockchainClientFactory(List<BlockchainClient> clients) {
        this.blockchainClients = clients.stream()
            .collect(Collectors.toMap(
                BlockchainClient::getNetworkType,
                Function.identity()
            ));
    }
    
    /**
     * 네트워크 타입에 따라 적절한 BlockchainClient를 반환합니다.
     * 
     * @param networkType 네트워크 타입
     * @return 해당 네트워크의 BlockchainClient
     * @throws IllegalArgumentException 지원하지 않는 네트워크 타입인 경우
     */
    public BlockchainClient getClient(NetworkType networkType) {
        BlockchainClient client = blockchainClients.get(networkType);
        if (client == null) {
            throw new IllegalArgumentException("Unsupported network type: " + networkType);
        }
        return client;
    }
} 