package com.bloominggrace.governance.point.domain.model;


import com.bloominggrace.governance.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "point_accounts")
@Getter
@NoArgsConstructor
public class PointAccount extends AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "frozen_balance", nullable = false)
    private BigDecimal frozenBalance;

    @Version
    private Long version;

    public PointAccount(UUID userId) {
        this.userId = userId;
        this.balance = BigDecimal.ZERO;
        this.frozenBalance = BigDecimal.ZERO;
    }

    public void earnPoints(PointAmount amount, String reason) {
        this.balance = this.balance.add(amount.getAmount());

    }

    public void freezePoints(PointAmount amount, String exchangeRequestId) {
        if (balance.compareTo(amount.getAmount()) >= 0) {
            this.balance = this.balance.subtract(amount.getAmount());
            this.frozenBalance = this.frozenBalance.add(amount.getAmount());

        } else {
            throw new IllegalArgumentException("동결할 포인트가 부족합니다");
        }
    }

    public void unfreezePoints(PointAmount amount, String exchangeRequestId) {
        if (frozenBalance.compareTo(amount.getAmount()) >= 0) {
            this.frozenBalance = this.frozenBalance.subtract(amount.getAmount());
            this.balance = this.balance.add(amount.getAmount());

        } else {
            throw new IllegalArgumentException("해제할 동결 포인트가 부족합니다");
        }
    }

    public PointAmount getAvailableBalance() {
        return PointAmount.of(balance);
    }

    public PointAmount getFrozenBalance() {
        return PointAmount.of(frozenBalance);
    }

    public PointAmount getTotalBalance() {
        return PointAmount.of(balance.add(frozenBalance));
    }
} 