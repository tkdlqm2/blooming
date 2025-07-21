package com.bloominggrace.governance.shared.infrastructure.service;

import com.bloominggrace.governance.shared.domain.service.RawTransactionBuilder;
import com.bloominggrace.governance.shared.infrastructure.service.ethereum.EthereumRawTransactionBuilder;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RawTransactionBuilder 팩토리
 * 네트워크 타입별로 적절한 RawTransactionBuilder를 제공합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RawTransactionBuilderFactory {
    
    private final EthereumRawTransactionBuilder ethereumRawTransactionBuilder;
    
    /**
     * 네트워크 타입에 따른 RawTransactionBuilder 반환
     * 
     * @param networkType 네트워크 타입
     * @return RawTransactionBuilder 구현체
     * @throws UnsupportedOperationException 지원하지 않는 네트워크인 경우
     */
    public RawTransactionBuilder getBuilder(NetworkType networkType) {
        switch (networkType) {
            case ETHEREUM:
                return ethereumRawTransactionBuilder;
            case SOLANA:
                // TODO: SolanaRawTransactionBuilder 구현 후 추가
                throw new UnsupportedOperationException("Solana RawTransactionBuilder is not implemented yet");
            default:
                throw new UnsupportedOperationException("Unsupported network type: " + networkType);
        }
    }
} 