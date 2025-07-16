package com.bloominggrace.governance.wallet.domain.service;

import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.domain.model.TransactionBody;
import com.bloominggrace.governance.shared.domain.model.SignedTransaction;

import java.util.List;
import java.util.Optional;

/**
 * 지갑 서비스 인터페이스
 * 지갑 생성, 조회, 관리 기능을 담당하는 도메인 서비스
 */
public interface WalletService {
    
    /**
     * 새로운 지갑을 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param networkType 네트워크 타입 (ETHEREUM, SOLANA 등)
     * @return 생성된 지갑
     */
    Wallet createWallet(UserId userId, NetworkType networkType);
    
    /**
     * 지갑 주소로 지갑을 조회합니다.
     * 
     * @param walletAddress 지갑 주소
     * @return 지갑 정보
     */
    Optional<Wallet> findByAddress(String walletAddress);
    
    /**
     * 사용자의 모든 지갑을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자의 지갑 목록
     */
    List<Wallet> findByUserId(UserId userId);
    
    /**
     * 지갑을 저장합니다.
     * 
     * @param wallet 저장할 지갑
     * @return 저장된 지갑
     */
    Wallet save(Wallet wallet);

    
    /**
     * 지갑 활성화 상태를 변경합니다.
     * 
     * @param walletAddress 지갑 주소
     * @param active 활성화 여부
     * @return 업데이트된 지갑
     */
    Wallet updateActiveStatus(String walletAddress, boolean active);


    /**
     * 주어진 메시지에 대해 개인키로 서명합니다.
     *
     * @param message 서명할 메시지 (byte[])
     * @param privateKey 개인키 (hex string)
     * @return 서명 결과 (byte[])
     */
    byte[] sign(byte[] message, String privateKey);

    /**
     * TransactionBody를 서명하여 인코딩된 signedRawTransaction을 반환합니다.
     * 각 네트워크별 구현체에서 구체적인 서명 및 인코딩 로직을 구현합니다.
     *
     * @param transactionBody 서명할 트랜잭션 본문
     * @param privateKey 개인키 (hex string)
     * @return 인코딩된 signedRawTransaction (byte[])
     */
    <T> byte[] signTransactionBody(TransactionBody<T> transactionBody, String privateKey);

    /**
     * 지갑 주소 유효성 검증
     * @param address 지갑 주소
     * @return 유효한 경우 true
     */
    boolean isValidAddress(String address);
} 