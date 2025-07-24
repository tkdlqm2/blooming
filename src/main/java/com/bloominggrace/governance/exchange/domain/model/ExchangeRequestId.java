package com.bloominggrace.governance.exchange.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@EqualsAndHashCode
public class ExchangeRequestId implements Serializable {
    @Column(name = "exchange_request_id", nullable = false, updatable = false)
    private UUID value;

    protected ExchangeRequestId() {}

    public ExchangeRequestId(UUID value) {
        this.value = value;
    }

    public static ExchangeRequestId of(UUID value) {
        return new ExchangeRequestId(value);
    }

    public static ExchangeRequestId generate() {
        return new ExchangeRequestId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    public void setValue(UUID value) {
        this.value = value;
    }
} 