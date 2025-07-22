package com.bloominggrace.governance.governance.domain.model;

import com.bloominggrace.governance.shared.domain.EntityId;
import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public class ProposalId extends EntityId {
    
    public ProposalId() {
        super(UUID.randomUUID());
    }
    
    public ProposalId(UUID value) {
        super(value);
    }
    
    public ProposalId(String value) {
        super(UUID.fromString(value));
    }
} 