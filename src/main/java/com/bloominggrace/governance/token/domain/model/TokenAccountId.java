package com.bloominggrace.governance.token.domain.model;

import com.bloominggrace.governance.shared.domain.EntityId;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import java.util.UUID;

@Embeddable
public class TokenAccountId extends EntityId {
    
    @Column(name = "id")
    private UUID id;
    
    public TokenAccountId() {
        super(UUID.randomUUID());
        this.id = getValue();
    }
    
    public TokenAccountId(UUID value) {
        super(value);
        this.id = value;
    }
    
    public TokenAccountId(String value) {
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