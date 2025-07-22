package com.bloominggrace.governance.governance.application.dto;

import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadcastProposalRequest {
    
    private String creatorWalletAddress;
    private BigDecimal proposalFee;
    private NetworkType networkType;
} 