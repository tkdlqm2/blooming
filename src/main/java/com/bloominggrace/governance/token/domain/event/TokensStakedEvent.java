package com.bloominggrace.governance.token.domain.event;

import com.bloominggrace.governance.shared.domain.DomainEvent;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenAmount;

import java.time.LocalDateTime;

public class TokensStakedEvent implements DomainEvent {
    private final UserId userId;
    private final TokenAmount amount;
    private final LocalDateTime occurredOn;

    public TokensStakedEvent(UserId userId, TokenAmount amount) {
        this.userId = userId;
        this.amount = amount;
        this.occurredOn = LocalDateTime.now();
    }

    public UserId getUserId() {
        return userId;
    }

    public TokenAmount getAmount() {
        return amount;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
} 