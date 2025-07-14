package com.bloominggrace.governance.exchange.domain.event;

import com.bloominggrace.governance.exchange.domain.model.ExchangeRequestId;
import com.bloominggrace.governance.point.domain.model.PointAmount;
import com.bloominggrace.governance.shared.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public class ExchangeCancelledEvent implements DomainEvent {
    private final ExchangeRequestId exchangeRequestId;
    private final UUID userId;
    private final PointAmount pointAmount;
    private final LocalDateTime occurredOn;

    public ExchangeCancelledEvent(ExchangeRequestId exchangeRequestId, UUID userId, PointAmount pointAmount) {
        this.exchangeRequestId = exchangeRequestId;
        this.userId = userId;
        this.pointAmount = pointAmount;
        this.occurredOn = LocalDateTime.now();
    }

    public ExchangeRequestId getExchangeRequestId() {
        return exchangeRequestId;
    }

    public UUID getUserId() {
        return userId;
    }

    public PointAmount getPointAmount() {
        return pointAmount;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
} 