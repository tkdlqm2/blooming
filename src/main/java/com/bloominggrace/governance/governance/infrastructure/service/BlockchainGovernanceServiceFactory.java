package com.bloominggrace.governance.governance.infrastructure.service;

import com.bloominggrace.governance.governance.domain.service.BlockchainGovernanceService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 블록체인 거버넌스 서비스 팩토리
 * 네트워크 타입에 따라 적절한 거버넌스 서비스를 반환
 */
@Slf4j
@Component
public class BlockchainGovernanceServiceFactory {
    
    private final Map<NetworkType, BlockchainGovernanceService> governanceServices;
    
    public BlockchainGovernanceServiceFactory(List<BlockchainGovernanceService> services) {
        this.governanceServices = services.stream()
            .collect(Collectors.toMap(
                BlockchainGovernanceService::getSupportedNetworkType,
                Function.identity()
            ));
        
        log.info("Initialized BlockchainGovernanceServiceFactory with {} services: {}", 
            governanceServices.size(), 
            governanceServices.keySet());
    }
    
    /**
     * 네트워크 타입에 따른 거버넌스 서비스 반환
     * 
     * @param networkType 네트워크 타입
     * @return 해당 네트워크의 거버넌스 서비스
     * @throws IllegalArgumentException 지원하지 않는 네트워크 타입인 경우
     */
    public BlockchainGovernanceService getService(NetworkType networkType) {
        BlockchainGovernanceService service = governanceServices.get(networkType);
        
        if (service == null) {
            throw new IllegalArgumentException(
                String.format("No governance service found for network type: %s. Available: %s", 
                    networkType, governanceServices.keySet())
            );
        }
        
        log.debug("Returning governance service for network type: {}", networkType);
        return service;
    }
    
    /**
     * 지원하는 네트워크 타입 목록 반환
     * 
     * @return 지원하는 네트워크 타입 목록
     */
    public List<NetworkType> getSupportedNetworkTypes() {
        return List.copyOf(governanceServices.keySet());
    }
    
    /**
     * 특정 네트워크 타입이 지원되는지 확인
     * 
     * @param networkType 확인할 네트워크 타입
     * @return 지원되는 경우 true
     */
    public boolean isSupported(NetworkType networkType) {
        return governanceServices.containsKey(networkType);
    }
} 