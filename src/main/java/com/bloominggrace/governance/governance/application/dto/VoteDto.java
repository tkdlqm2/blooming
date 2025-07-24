package com.bloominggrace.governance.governance.application.dto;

import com.bloominggrace.governance.governance.domain.model.Vote;
import com.bloominggrace.governance.governance.domain.model.VoteId;
import com.bloominggrace.governance.governance.domain.model.VoteType;
import com.bloominggrace.governance.shared.domain.UserId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class VoteDto {
    private final UUID id;
    private final UUID proposalId;
    private final UUID voterId;
    private final String voteType;
    private final long votingPower;
    private final String reason;
    private final LocalDateTime createdAt;
    private final String transactionSignature;
    private final String voterWalletAddress;

    public VoteDto(UUID id, UUID proposalId, UUID voterId, String voteType, long votingPower,
                   String reason, LocalDateTime createdAt, String transactionSignature, String voterWalletAddress) {
        this.id = id;
        this.proposalId = proposalId;
        this.voterId = voterId;
        this.voteType = voteType;
        this.votingPower = votingPower;
        this.reason = reason;
        this.createdAt = createdAt;
        this.transactionSignature = transactionSignature;
        this.voterWalletAddress = voterWalletAddress;
    }

    public static VoteDto from(Vote vote) {
        return new VoteDto(
            vote.getId().getValue(),
            vote.getProposalId().getValue(),
            vote.getVoterId().getValue(),
            vote.getVoteType().name(),
            vote.getVotingPower(),
            vote.getReason(),
            vote.getCreatedAt(),
            null, // transactionSignature는 별도로 설정
            null  // voterWalletAddress는 별도로 설정
        );
    }

    public static VoteDto from(Vote vote, String transactionSignature, String voterWalletAddress) {
        return new VoteDto(
            vote.getId().getValue(),
            vote.getProposalId().getValue(),
            vote.getVoterId().getValue(),
            vote.getVoteType().name(),
            vote.getVotingPower(),
            vote.getReason(),
            vote.getCreatedAt(),
            transactionSignature,
            voterWalletAddress
        );
    }
} 