package com.bloominggrace.governance.shared.infrastructure.service;

import com.bloominggrace.governance.shared.domain.service.TransactionBuilder;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 네트워크별로 적절한 TransactionBuilder를 반환하는 팩토리
 */
@Service
public class TransactionBuilderFactory {

    private final Map<NetworkType, TransactionBuilder> builders;

    @Autowired
    public TransactionBuilderFactory(EthereumTransactionBuilder ethereumBuilder,
                                   SolanaTransactionBuilder solanaBuilder) {
        this.builders = Map.of(
                NetworkType.ETHEREUM, ethereumBuilder,
                NetworkType.SOLANA, solanaBuilder
        );
    }

    /**
     * 네트워크 타입에 따른 TransactionBuilder를 반환합니다.
     * 
     * @param networkType 네트워크 타입
     * @return 해당 네트워크의 TransactionBuilder
     * @throws IllegalArgumentException 지원하지 않는 네트워크 타입인 경우
     */
    public TransactionBuilder getBuilder(NetworkType networkType) {
        TransactionBuilder builder = builders.get(networkType);
        if (builder == null) {
            throw new IllegalArgumentException("Unsupported network type: " + networkType);
        }
        return builder;
    }

    /**
     * 지원하는 네트워크 타입들을 반환합니다.
     * 
     * @return 지원하는 네트워크 타입들의 Set
     */
    public java.util.Set<NetworkType> getSupportedNetworks() {
        return builders.keySet();
    }
} 