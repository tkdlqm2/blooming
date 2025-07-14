package com.bloominggrace.governance.exchange.domain.event;

import com.bloominggrace.governance.exchange.domain.model.ExchangeRequestId;
import com.bloominggrace.governance.point.domain.model.PointAmount;
import com.bloominggrace.governance.shared.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public class ExchangeCompletedEvent implements DomainEvent {
    private final ExchangeRequestId exchangeRequestId;
    private final UUID userId;
    private final PointAmount pointAmount;
    private final String transactionSignature;
    private final LocalDateTime occurredOn;

    public ExchangeCompletedEvent(ExchangeRequestId exchangeRequestId, UUID userId, PointAmount pointAmount, String transactionSignature) {
        this.exchangeRequestId = exchangeRequestId;
        this.userId = userId;
        this.pointAmount = pointAmount;
        this.transactionSignature = transactionSignature;
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

    public String getTransactionSignature() {
        return transactionSignature;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
} 