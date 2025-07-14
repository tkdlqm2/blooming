package com.bloominggrace.governance.token.domain.event;

import com.bloominggrace.governance.shared.domain.DomainEvent;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenAmount;

import java.time.LocalDateTime;

public class TokensBurnedEvent implements DomainEvent {
    private final UserId userId;
    private final TokenAmount amount;
    private final String description;
    private final LocalDateTime occurredAt;
    
    public TokensBurnedEvent(UserId userId, TokenAmount amount, String description) {
        this.userId = userId;
        this.amount = amount;
        this.description = description;
        this.occurredAt = LocalDateTime.now();
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public TokenAmount getAmount() {
        return amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public LocalDateTime getOccurredOn() {
        return occurredAt;
    }
} 