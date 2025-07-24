package com.bloominggrace.governance.shared.blockchain.domain.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 트랜잭션 본문 팩토리 프로바이더
 * 네트워크 타입에 따라 적절한 팩토리를 제공합니다
 */
@Component
public class TransactionBodyFactoryProvider {
    
    private final Map<String, TransactionBodyFactory> factoryMap;
    
    public TransactionBodyFactoryProvider(List<TransactionBodyFactory> factories) {
        this.factoryMap = factories.stream()
            .collect(Collectors.toMap(
                TransactionBodyFactory::getSupportedNetworkType,
                Function.identity()
            ));
    }
    
    /**
     * 네트워크 타입에 따른 적절한 팩토리를 반환합니다
     * 
     * @param networkType 네트워크 타입 (ETHEREUM, SOLANA 등)
     * @return 해당 네트워크의 트랜잭션 본문 팩토리
     * @throws IllegalArgumentException 지원하지 않는 네트워크 타입인 경우
     */
    public TransactionBodyFactory getFactory(String networkType) {
        TransactionBodyFactory factory = factoryMap.get(networkType.toUpperCase());
        if (factory == null) {
            throw new IllegalArgumentException("Unsupported network type: " + networkType);
        }
        return factory;
    }
    
    /**
     * 지원하는 모든 네트워크 타입을 반환합니다
     * 
     * @return 지원하는 네트워크 타입 목록
     */
    public List<String> getSupportedNetworkTypes() {
        return List.copyOf(factoryMap.keySet());
    }
} 