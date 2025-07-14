package com.bloominggrace.governance.token.application.service;

import com.bloominggrace.governance.token.domain.service.TokenManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 토큰 관리 서비스 팩토리
 * 현재는 단일 TokenManagementService를 반환
 */
@Component
@RequiredArgsConstructor
public class TokenManagementServiceFactory {
    
    private final TokenManagementService tokenManagementService;
    
    /**
     * 토큰 관리 서비스를 반환합니다.
     * 
     * @return 토큰 관리 서비스
     */
    public TokenManagementService getTokenManagementService() {
        return tokenManagementService;
    }
    
    /**
     * 네트워크 타입에 따른 토큰 관리 서비스를 반환합니다.
     * 현재는 모든 네트워크에 대해 동일한 서비스를 반환합니다.
     * 
     * @param networkType 네트워크 타입 (ETHEREUM, SOLANA)
     * @return 토큰 관리 서비스
     */
    public TokenManagementService getTokenManagementService(String networkType) {
        return tokenManagementService;
    }
} 