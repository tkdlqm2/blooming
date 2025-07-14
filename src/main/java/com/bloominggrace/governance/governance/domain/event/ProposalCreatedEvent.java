package com.bloominggrace.governance.governance.domain.event;

import com.bloominggrace.governance.shared.domain.DomainEvent;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.governance.domain.model.ProposalId;

import java.time.LocalDateTime;

public class ProposalCreatedEvent implements DomainEvent {
    private final ProposalId proposalId;
    private final UserId creatorId;
    private final String title;
    private final LocalDateTime occurredOn;

    public ProposalCreatedEvent(ProposalId proposalId, UserId creatorId, String title) {
        this.proposalId = proposalId;
        this.creatorId = creatorId;
        this.title = title;
        this.occurredOn = LocalDateTime.now();
    }

    public ProposalId getProposalId() {
        return proposalId;
    }

    public UserId getCreatorId() {
        return creatorId;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
} 