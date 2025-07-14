package com.bloominggrace.governance.point.domain.event;

import com.bloominggrace.governance.point.domain.model.PointAmount;
import com.bloominggrace.governance.shared.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public class PointsEarnedEvent implements DomainEvent {
    private final UUID userId;
    private final PointAmount amount;
    private final String reason;
    private final LocalDateTime occurredOn;

    public PointsEarnedEvent(UUID userId, PointAmount amount, String reason) {
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
        this.occurredOn = LocalDateTime.now();
    }

    public UUID getUserId() {
        return userId;
    }

    public PointAmount getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
} 