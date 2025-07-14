package com.bloominggrace.governance.token.domain.model;

import com.bloominggrace.governance.shared.domain.ValueObject;

import java.math.BigDecimal;
import java.util.Objects;

public class TokenAmount extends ValueObject {
    private final BigDecimal amount;

    public TokenAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Token amount must be non-negative");
        }
        this.amount = amount;
    }

    public TokenAmount(String amount) {
        this(new BigDecimal(amount));
    }

    public TokenAmount(double amount) {
        this(BigDecimal.valueOf(amount));
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TokenAmount add(TokenAmount other) {
        return new TokenAmount(this.amount.add(other.amount));
    }

    public TokenAmount subtract(TokenAmount other) {
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Token amount cannot be negative");
        }
        return new TokenAmount(result);
    }

    public boolean isGreaterThan(TokenAmount other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqual(TokenAmount other) {
        return this.amount.compareTo(other.amount) >= 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TokenAmount that = (TokenAmount) obj;
        return Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return amount.toString();
    }
} 