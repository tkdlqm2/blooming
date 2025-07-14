package com.bloominggrace.governance.governance.application.dto;

import com.bloominggrace.governance.governance.domain.model.Vote;
import com.bloominggrace.governance.governance.domain.model.VoteType;

import java.time.LocalDateTime;
import java.util.UUID;

public class VoteDto {
    private final UUID id;
    private final UUID proposalId;
    private final UUID voterId;
    private final String voteType;
    private final long votingPower;
    private final String reason;
    private final LocalDateTime createdAt;

    public VoteDto(
            UUID id,
            UUID proposalId,
            UUID voterId,
            String voteType,
            long votingPower,
            String reason,
            LocalDateTime createdAt) {
        this.id = id;
        this.proposalId = proposalId;
        this.voterId = voterId;
        this.voteType = voteType;
        this.votingPower = votingPower;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static VoteDto from(Vote vote) {
        return new VoteDto(
            vote.getId().getValue(),
            vote.getProposalId().getValue(),
            vote.getVoterId().getValue(),
            vote.getVoteType().name(),
            vote.getVotingPower(),
            vote.getReason(),
            vote.getCreatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getProposalId() {
        return proposalId;
    }

    public UUID getVoterId() {
        return voterId;
    }

    public String getVoteType() {
        return voteType;
    }

    public long getVotingPower() {
        return votingPower;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
} 