package com.bloominggrace.governance.point.domain.event;

import com.bloominggrace.governance.point.domain.model.PointAmount;
import com.bloominggrace.governance.shared.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public class PointsUnfrozenEvent implements DomainEvent {
    private final UUID userId;
    private final PointAmount amount;
    private final String exchangeRequestId;
    private final LocalDateTime occurredOn;

    public PointsUnfrozenEvent(UUID userId, PointAmount amount, String exchangeRequestId) {
        this.userId = userId;
        this.amount = amount;
        this.exchangeRequestId = exchangeRequestId;
        this.occurredOn = LocalDateTime.now();
    }

    public UUID getUserId() {
        return userId;
    }

    public PointAmount getAmount() {
        return amount;
    }

    public String getExchangeRequestId() {
        return exchangeRequestId;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
} 