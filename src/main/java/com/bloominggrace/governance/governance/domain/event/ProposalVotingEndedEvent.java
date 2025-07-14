package com.bloominggrace.governance.governance.domain.event;

import com.bloominggrace.governance.shared.domain.DomainEvent;
import com.bloominggrace.governance.governance.domain.model.ProposalId;
import com.bloominggrace.governance.governance.domain.model.ProposalStatus;
import com.bloominggrace.governance.governance.domain.model.VoteResults;

import java.time.LocalDateTime;

public class ProposalVotingEndedEvent implements DomainEvent {
    private final ProposalId proposalId;
    private final ProposalStatus finalStatus;
    private final VoteResults voteResults;
    private final LocalDateTime occurredOn;

    public ProposalVotingEndedEvent(ProposalId proposalId, ProposalStatus finalStatus, VoteResults voteResults) {
        this.proposalId = proposalId;
        this.finalStatus = finalStatus;
        this.voteResults = voteResults;
        this.occurredOn = LocalDateTime.now();
    }

    public ProposalId getProposalId() {
        return proposalId;
    }

    public ProposalStatus getFinalStatus() {
        return finalStatus;
    }

    public VoteResults getVoteResults() {
        return voteResults;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
} 