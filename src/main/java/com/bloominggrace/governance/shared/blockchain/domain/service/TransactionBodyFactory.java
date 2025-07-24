package com.bloominggrace.governance.shared.blockchain.domain.service;

/**
 * 트랜잭션 본문 생성을 위한 팩토리 인터페이스
 * 네트워크별로 다른 구현체를 제공합니다
 */
public interface TransactionBodyFactory {
    
    /**
     * 트랜잭션 요청을 받아서 네트워크별 특화된 TransactionBody를 생성합니다
     * 
     * @param request 트랜잭션 생성 요청
     * @return 네트워크별 특화된 TransactionBody
     */
    <T> com.bloominggrace.governance.shared.blockchain.domain.model.TransactionBody<T> createTransactionBody(com.bloominggrace.governance.shared.blockchain.domain.model.TransactionRequest request);
    
    /**
     * 이 팩토리가 지원하는 네트워크 타입을 반환합니다
     * 
     * @return 지원하는 네트워크 타입
     */
    String getSupportedNetworkType();
} 