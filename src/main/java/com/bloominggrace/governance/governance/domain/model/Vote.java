package com.bloominggrace.governance.governance.domain.model;

import com.bloominggrace.governance.shared.domain.ValueObject;
import com.bloominggrace.governance.shared.domain.UserId;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "votes")
public class Vote extends ValueObject {
    
    @EmbeddedId
    private VoteId id;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "proposal_id"))
    })
    private ProposalId proposalId;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "voter_id"))
    })
    private UserId voterId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false)
    private VoteType voteType;
    
    @Column(name = "voting_power", nullable = false)
    private long votingPower;
    
    @Column(name = "reason")
    private String reason;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Vote() {}

    public Vote(ProposalId proposalId, UserId voterId, VoteType voteType, long votingPower, String reason) {
        this.id = new VoteId();
        this.proposalId = proposalId;
        this.voterId = voterId;
        this.voteType = voteType;
        this.votingPower = votingPower;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }

    public VoteId getId() {
        return id;
    }

    public ProposalId getProposalId() {
        return proposalId;
    }

    public UserId getVoterId() {
        return voterId;
    }

    public VoteType getVoteType() {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vote vote = (Vote) obj;
        return Objects.equals(id, vote.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Vote{id=%s, proposalId=%s, voterId=%s, voteType=%s, votingPower=%d}",
                           id, proposalId, voterId, voteType, votingPower);
    }
} 