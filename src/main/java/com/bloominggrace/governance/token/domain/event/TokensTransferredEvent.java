package com.bloominggrace.governance.token.domain.event;

import com.bloominggrace.governance.shared.domain.DomainEvent;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenAmount;

import java.time.LocalDateTime;

public class TokensTransferredEvent implements DomainEvent {
    private final UserId fromUserId;
    private final String toWalletAddress;
    private final TokenAmount amount;
    private final String description;
    private final LocalDateTime occurredAt;
    
    public TokensTransferredEvent(UserId fromUserId, String toWalletAddress, TokenAmount amount, String description) {
        this.fromUserId = fromUserId;
        this.toWalletAddress = toWalletAddress;
        this.amount = amount;
        this.description = description;
        this.occurredAt = LocalDateTime.now();
    }
    
    public UserId getFromUserId() {
        return fromUserId;
    }
    
    public String getToWalletAddress() {
        return toWalletAddress;
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