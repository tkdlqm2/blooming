package com.bloominggrace.governance.governance.application.dto;

import com.bloominggrace.governance.governance.domain.model.VoteType;
import com.bloominggrace.governance.point.domain.model.PointAmount;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {
    private UUID voterId;
    private VoteType voteType;
    private String reason;
    private String voterWalletAddress;
    private PointAmount votingPower;
    private NetworkType networkType;
} 