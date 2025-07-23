package com.bloominggrace.governance.governance.application.dto;

import com.bloominggrace.governance.governance.domain.model.VoteType;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CastVoteRequest {
    
    private UUID voterId;
    private VoteType voteType; // FOR, AGAINST, ABSTAIN
    private String reason; // 선택적
    private String voterWalletAddress;
    private NetworkType networkType;
    
    // 투표 파워는 서버에서 자동 계산 (토큰 잔액 기반)
} 