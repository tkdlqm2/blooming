package com.bloominggrace.governance.shared.domain.service;

import com.bloominggrace.governance.wallet.domain.model.NetworkType;

import java.math.BigDecimal;

/**
 * 각 네트워크별로 트랜잭션 바디를 생성하는 빌더 인터페이스
 */
public interface TransactionBuilder {
    
    /**
     * 트랜잭션 바디를 생성합니다.
     * 
     * @param fromAddress 보내는 주소 (Admin인 경우 AdminWalletService 활용)
     * @param toAddress 받는 주소
     * @param value 전송할 값
     * @param networkType 네트워크 타입
     * @return 생성된 트랜잭션 바디
     */
    Object buildTransactionBody(String fromAddress, String toAddress, BigDecimal value, NetworkType networkType);
    
    /**
     * 토큰 민팅을 위한 트랜잭션 바디를 생성합니다.
     * 
     * @param fromAddress 보내는 주소 (Admin인 경우 AdminWalletService 활용)
     * @param toAddress 받는 주소
     * @param tokenAmount 토큰 양
     * @param tokenContractAddress 토큰 컨트랙트 주소
     * @param networkType 네트워크 타입
     * @return 생성된 트랜잭션 바디
     */
    Object buildMintTransactionBody(String fromAddress, String toAddress, BigDecimal tokenAmount, String tokenContractAddress, NetworkType networkType);
} 