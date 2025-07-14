package com.bloominggrace.governance.token.domain.event;

import com.bloominggrace.governance.shared.domain.DomainEvent;
import com.bloominggrace.governance.shared.domain.UserId;

import java.time.LocalDateTime;

public class TokenAccountDeactivatedEvent implements DomainEvent {
    private final UserId userId;
    private final String walletAddress;
    private final LocalDateTime occurredOn;

    public TokenAccountDeactivatedEvent(UserId userId, String walletAddress) {
        this.userId = userId;
        this.walletAddress = walletAddress;
        this.occurredOn = LocalDateTime.now();
    }

    public UserId getUserId() {
        return userId;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
} 