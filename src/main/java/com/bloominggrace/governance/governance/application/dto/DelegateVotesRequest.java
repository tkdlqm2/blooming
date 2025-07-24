package com.bloominggrace.governance.governance.application.dto;

import lombok.Data;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;

@Data
public class DelegateVotesRequest {
    private String delegateeWalletAddress;  // 위임받는 주소
    private NetworkType networkType;
} 