package com.bloominggrace.governance.token.application.dto;

import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 토큰 계정 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTokenAccountRequest {
    private String walletAddress;
    private String userId;
    private NetworkType network;
    private String contract;
    private String symbol;
} 