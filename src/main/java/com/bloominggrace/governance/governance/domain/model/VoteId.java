package com.bloominggrace.governance.governance.domain.model;

import com.bloominggrace.governance.shared.domain.EntityId;
import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public class VoteId extends EntityId {
    
    public VoteId() {
        super(UUID.randomUUID());
    }
    
    public VoteId(UUID value) {
        super(value);
    }
    
    public VoteId(String value) {
        super(UUID.fromString(value));
    }
} 