package com.bloominggrace.governance.governance.application.dto;

import com.bloominggrace.governance.governance.domain.model.Proposal;
import com.bloominggrace.governance.governance.domain.model.ProposalStatus;
import com.bloominggrace.governance.governance.domain.model.VoteResults;
import com.bloominggrace.governance.governance.domain.model.VotingPeriod;
import lombok.Builder;
import lombok.Getter;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ProposalDto {
    private final UUID id;
    private final UUID creatorId;
    private final String title;
    private final String description;
    private final String status;
    private final VotingPeriodDto votingPeriod;
    private final VoteResultsDto voteResults;
    private final long requiredQuorum;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String txHash;
    private final String creatorWalletAddress;
    private final BigInteger proposalCount;

    public static ProposalDto from(Proposal proposal) {
        return ProposalDto.builder()
            .id(proposal.getId().getValue())
            .creatorId(proposal.getCreatorId() != null ? proposal.getCreatorId().getValue() : null)
            .title(proposal.getTitle())
            .description(proposal.getDescription())
            .status(proposal.getStatus().name())
            .votingPeriod(VotingPeriodDto.from(proposal.getVotingPeriod()))
            .voteResults(VoteResultsDto.from(proposal.getVoteResults()))
            .requiredQuorum(proposal.getRequiredQuorum())
            .createdAt(proposal.getCreatedAt())
            .updatedAt(proposal.getUpdatedAt())
            .txHash(proposal.getTxHash())
            .creatorWalletAddress(proposal.getCreatorWalletAddress())
            .proposalCount(proposal.getProposalCount())
            .build();
    }
    
    public static ProposalDto from(Proposal proposal, String transactionSignature, String creatorWalletAddress) {
        return ProposalDto.builder()
            .id(proposal.getId().getValue())
            .creatorId(proposal.getCreatorId() != null ? proposal.getCreatorId().getValue() : null)
            .title(proposal.getTitle())
            .description(proposal.getDescription())
            .status(proposal.getStatus().name())
            .votingPeriod(VotingPeriodDto.from(proposal.getVotingPeriod()))
            .voteResults(VoteResultsDto.from(proposal.getVoteResults()))
            .requiredQuorum(proposal.getRequiredQuorum())
            .createdAt(proposal.getCreatedAt())
            .updatedAt(proposal.getUpdatedAt())
            .txHash(transactionSignature)
            .creatorWalletAddress(creatorWalletAddress)
            .build();
    }
} 