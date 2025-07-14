package com.bloominggrace.governance.wallet.domain.service;

import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.shared.domain.UserId;

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
    Wallet createWallet(UserId userId, String networkType);
    
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
     * 지갑을 삭제합니다.
     * 
     * @param walletAddress 지갑 주소
     */
    void deleteByAddress(String walletAddress);
    
    /**
     * 지갑 활성화 상태를 변경합니다.
     * 
     * @param walletAddress 지갑 주소
     * @param active 활성화 여부
     * @return 업데이트된 지갑
     */
    Wallet updateActiveStatus(String walletAddress, boolean active);
    
    /**
     * 지원하는 네트워크 타입을 반환합니다.
     * 
     * @return 네트워크 타입
     */
    String getSupportedNetworkType();

    /**
     * 주어진 메시지에 대해 개인키로 서명합니다.
     *
     * @param message 서명할 메시지 (byte[])
     * @param privateKey 개인키 (hex string)
     * @return 서명 결과 (byte[])
     */
    byte[] sign(byte[] message, String privateKey);
} 