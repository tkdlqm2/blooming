package com.bloominggrace.governance.governance.domain.model;

import com.bloominggrace.governance.shared.domain.EntityId;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import java.util.UUID;

@Embeddable
public class VoteId extends EntityId {
    
    @Column(name = "vote_id")
    private UUID id;
    
    public VoteId() {
        super(UUID.randomUUID());
        this.id = getValue();
    }
    
    public VoteId(UUID value) {
        super(value);
        this.id = value;
    }
    
    public VoteId(String value) {
        super(UUID.fromString(value));
        this.id = getValue();
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
} 