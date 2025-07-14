package com.bloominggrace.governance.exchange.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeRequestId that = (ExchangeRequestId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
} 