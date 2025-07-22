package com.bloominggrace.governance.governance.domain.model;

import com.bloominggrace.governance.shared.domain.AggregateRoot;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.governance.domain.event.ProposalActivatedEvent;
import com.bloominggrace.governance.governance.domain.event.ProposalCreatedEvent;
import com.bloominggrace.governance.governance.domain.event.ProposalVotingEndedEvent;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "proposals")
public class Proposal extends AggregateRoot {
    
    @EmbeddedId
    private ProposalId id;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "creator_id"))
    })
    private UserId creatorId;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProposalStatus status;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "startDate", column = @Column(name = "voting_start_date")),
        @AttributeOverride(name = "endDate", column = @Column(name = "voting_end_date"))
    })
    private VotingPeriod votingPeriod;
    
    @Embedded
    private VoteResults voteResults;
    
    @Column(name = "required_quorum")
    private long requiredQuorum;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Proposal() {}

    public Proposal(
            UserId creatorId,
            String title,
            String description,
            VotingPeriod votingPeriod,
            long requiredQuorum) {
        this.id = new ProposalId();
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.status = ProposalStatus.DRAFT;
        this.votingPeriod = votingPeriod;
        this.requiredQuorum = requiredQuorum;
        this.voteResults = new VoteResults(0, 0, 0, 0);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new ProposalCreatedEvent(id, creatorId, title));
    }

    public ProposalId getId() {
        return id;
    }

    public UserId getCreatorId() {
        return creatorId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public VotingPeriod getVotingPeriod() {
        return votingPeriod;
    }

    public VoteResults getVoteResults() {
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

    public void activate() {
        if (this.status != ProposalStatus.DRAFT) {
            throw new IllegalStateException("Only draft proposals can be activated");
        }
        
        this.status = ProposalStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new ProposalActivatedEvent(id, creatorId, title));
    }

    public void startVoting() {
        if (this.status != ProposalStatus.ACTIVE) {
            throw new IllegalStateException("Only active proposals can start voting");
        }
        
        if (!votingPeriod.isVotingActive()) {
            throw new IllegalStateException("Voting period is not active yet");
        }
        
        this.status = ProposalStatus.VOTING;
        this.updatedAt = LocalDateTime.now();
    }

    public void addVote(VoteType voteType, long votingPower) {
        if (this.status != ProposalStatus.VOTING) {
            throw new IllegalStateException("Voting is not active for this proposal");
        }
        
        if (votingPeriod.isVotingEnded()) {
            throw new IllegalStateException("Voting period has ended");
        }
        
        long currentTotal = voteResults.getTotalVotes();
        long currentYes = voteResults.getYesVotes();
        long currentNo = voteResults.getNoVotes();
        long currentAbstain = voteResults.getAbstainVotes();
        
        switch (voteType) {
            case YES:
                this.voteResults = new VoteResults(currentTotal + votingPower, currentYes + votingPower, currentNo, currentAbstain);
                break;
            case NO:
                this.voteResults = new VoteResults(currentTotal + votingPower, currentYes, currentNo + votingPower, currentAbstain);
                break;
            case ABSTAIN:
                this.voteResults = new VoteResults(currentTotal + votingPower, currentYes, currentNo, currentAbstain + votingPower);
                break;
        }
        
        this.updatedAt = LocalDateTime.now();
    }

    public void endVoting() {
        if (this.status != ProposalStatus.VOTING) {
            throw new IllegalStateException("Proposal is not in voting status");
        }
        
        if (!votingPeriod.isVotingEnded()) {
            throw new IllegalStateException("Voting period has not ended yet");
        }
        
        if (voteResults.hasQuorum(requiredQuorum)) {
            this.status = voteResults.isPassed() ? ProposalStatus.PASSED : ProposalStatus.REJECTED;
        } else {
            this.status = ProposalStatus.REJECTED; // 쿼럼 미달로 거부
        }
        
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new ProposalVotingEndedEvent(id, status, voteResults));
    }

    public void updateTitle(String newTitle) {
        if (this.status != ProposalStatus.DRAFT) {
            throw new IllegalStateException("Only draft proposals can be updated");
        }
        
        this.title = newTitle;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateDescription(String newDescription) {
        if (this.status != ProposalStatus.DRAFT) {
            throw new IllegalStateException("Only draft proposals can be updated");
        }
        
        this.description = newDescription;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canVote() {
        return this.status == ProposalStatus.VOTING && votingPeriod.isVotingActive();
    }

    public boolean isVotingActive() {
        return this.status == ProposalStatus.VOTING;
    }

    public boolean isPassed() {
        return this.status == ProposalStatus.PASSED;
    }

    public boolean isRejected() {
        return this.status == ProposalStatus.REJECTED;
    }

    @Override
    public String toString() {
        return String.format("Proposal{id=%s, title='%s', status=%s, votingPeriod=%s, voteResults=%s}",
                           id, title, status, votingPeriod, voteResults);
    }
} 