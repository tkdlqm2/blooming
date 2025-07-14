package com.bloominggrace.governance.governance.application.dto;

import com.bloominggrace.governance.governance.domain.model.Proposal;
import com.bloominggrace.governance.governance.domain.model.ProposalStatus;
import com.bloominggrace.governance.governance.domain.model.VoteResults;
import com.bloominggrace.governance.governance.domain.model.VotingPeriod;

import java.time.LocalDateTime;
import java.util.UUID;

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

    public ProposalDto(
            UUID id,
            UUID creatorId,
            String title,
            String description,
            String status,
            VotingPeriodDto votingPeriod,
            VoteResultsDto voteResults,
            long requiredQuorum,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.votingPeriod = votingPeriod;
        this.voteResults = voteResults;
        this.requiredQuorum = requiredQuorum;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProposalDto from(Proposal proposal) {
        return new ProposalDto(
            proposal.getId().getValue(),
            proposal.getCreatorId().getValue(),
            proposal.getTitle(),
            proposal.getDescription(),
            proposal.getStatus().name(),
            VotingPeriodDto.from(proposal.getVotingPeriod()),
            VoteResultsDto.from(proposal.getVoteResults()),
            proposal.getRequiredQuorum(),
            proposal.getCreatedAt(),
            proposal.getUpdatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public VotingPeriodDto getVotingPeriod() {
        return votingPeriod;
    }

    public VoteResultsDto getVoteResults() {
        return voteResults;
    }

    public long getRequiredQuorum() {
        return requiredQuorum;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
} 