package com.bloominggrace.governance.point.domain.model;

import com.bloominggrace.governance.shared.domain.ValueObject;

import java.math.BigDecimal;
import java.util.Objects;

public class PointAmount extends ValueObject {
    private final BigDecimal amount;

    public PointAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("포인트 금액은 0 이상이어야 합니다");
        }
        this.amount = amount;
    }

    public static PointAmount of(BigDecimal amount) {
        return new PointAmount(amount);
    }

    public static PointAmount of(int amount) {
        return new PointAmount(BigDecimal.valueOf(amount));
    }

    public static PointAmount zero() {
        return new PointAmount(BigDecimal.ZERO);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PointAmount add(PointAmount other) {
        return new PointAmount(this.amount.add(other.amount));
    }

    public PointAmount subtract(PointAmount other) {
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("포인트가 부족합니다");
        }
        return new PointAmount(result);
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isGreaterThan(PointAmount other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PointAmount that = (PointAmount) obj;
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