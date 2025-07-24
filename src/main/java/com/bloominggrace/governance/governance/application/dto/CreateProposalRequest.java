package com.bloominggrace.governance.governance.application.dto;

import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProposalRequest {
    private UUID creatorId;
    private String title;
    private String description;
    private LocalDateTime votingStartDate;
    private LocalDateTime votingEndDate;
    private BigDecimal requiredQuorum;
    private String creatorWalletAddress;
    private BigDecimal proposalFee;
    private NetworkType networkType;
} 