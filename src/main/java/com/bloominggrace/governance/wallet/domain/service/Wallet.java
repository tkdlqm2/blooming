package com.bloominggrace.governance.wallet.domain.service;

import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.shared.domain.UserId;

import java.util.List;
import java.util.Optional;

/**
 * 지갑 인터페이스
 * 지갑 생성, 조회, 관리 및 트랜잭션 관련 기능을 담당하는 도메인 서비스
 */
public interface Wallet {
    
    /**
     * 새로운 지갑을 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param networkType 네트워크 타입 (ETHEREUM, SOLANA 등)
     * @return 생성된 지갑
     */
    com.bloominggrace.governance.wallet.domain.model.Wallet createWallet(UserId userId, NetworkType networkType);
    
    /**
     * 지갑 주소로 지갑을 조회합니다.
     * 
     * @param walletAddress 지갑 주소
     * @return 지갑 정보
     */
    Optional<com.bloominggrace.governance.wallet.domain.model.Wallet> findByAddress(String walletAddress);
    
    /**
     * 사용자의 모든 지갑을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자의 지갑 목록
     */
    List<com.bloominggrace.governance.wallet.domain.model.Wallet> findByUserId(UserId userId);
    
    /**
     * 지갑을 저장합니다.
     * 
     * @param wallet 저장할 지갑
     * @return 저장된 지갑
     */
    com.bloominggrace.governance.wallet.domain.model.Wallet save(com.bloominggrace.governance.wallet.domain.model.Wallet wallet);

    /**
     * 지갑 활성화 상태를 변경합니다.
     * 
     * @param walletAddress 지갑 주소
     * @param active 활성화 여부
     * @return 업데이트된 지갑
     */
    com.bloominggrace.governance.wallet.domain.model.Wallet updateActiveStatus(String walletAddress, boolean active);

    /**
     * 주어진 메시지에 대해 개인키로 서명합니다.
     *
     * @param message 서명할 메시지 (byte[])
     * @param privateKey 개인키 (hex string)
     * @return 서명 결과 (byte[])
     */
    byte[] sign(byte[] message, String privateKey);

    /**
     * 원시 트랜잭션을 생성합니다.
     * 
     * @param fromAddress 보내는 주소
     * @param toAddress 받는 주소
     * @param amount 전송할 금액
     * @param data 트랜잭션 데이터
     * @param nonce 논스
     * @return 생성된 원시 트랜잭션 (byte[])
     */
    byte[] createRawTransaction(String fromAddress, String toAddress, String amount, String data, String nonce);

    /**
     * 원시 트랜잭션에 서명합니다.
     * 
     * @param rawTransaction 원시 트랜잭션 (byte[])
     * @param privateKey 개인키 (hex string)
     * @return 서명된 트랜잭션 (byte[])
     */
    byte[] signRawTransaction(byte[] rawTransaction, String privateKey);

    /**
     * 새로운 계정을 생성합니다.
     * 
     * @param password 계정 비밀번호
     * @return 생성된 계정 주소
     */
    String createAccount(String password);

    /**
     * 계정을 잠금 해제합니다.
     * 
     * @param address 계정 주소
     * @param password 계정 비밀번호
     * @return 잠금 해제 성공 여부
     */
    boolean unlockAccount(String address, String password);
} 