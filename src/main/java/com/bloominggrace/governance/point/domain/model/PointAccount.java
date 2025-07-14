package com.bloominggrace.governance.point.domain.model;

import com.bloominggrace.governance.point.domain.event.PointsEarnedEvent;
import com.bloominggrace.governance.point.domain.event.PointsFrozenEvent;
import com.bloominggrace.governance.point.domain.event.PointsUnfrozenEvent;
import com.bloominggrace.governance.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
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

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "balance", nullable = false))
    })
    private PointAmount balance;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "frozen_balance", nullable = false))
    })
    private PointAmount frozenBalance;

    @Version
    private Long version;

    @OneToMany(mappedBy = "pointAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PointTransaction> transactions = new ArrayList<>();

    public PointAccount(UUID userId) {
        this.userId = userId;
        this.balance = PointAmount.zero();
        this.frozenBalance = PointAmount.zero();
    }

    public void earnPoints(PointAmount amount, String reason) {
        this.balance = this.balance.add(amount);
        PointTransaction transaction = new PointTransaction(userId, PointTransactionType.EARN, amount, reason);
        transaction.setPointAccount(this);
        this.transactions.add(transaction);
        addDomainEvent(new PointsEarnedEvent(userId, amount, reason));
    }

    public void freezePoints(PointAmount amount, String exchangeRequestId) {
        if (balance.isGreaterThan(amount) || balance.equals(amount)) {
            this.balance = this.balance.subtract(amount);
            this.frozenBalance = this.frozenBalance.add(amount);
            PointTransaction transaction = new PointTransaction(userId, PointTransactionType.FREEZE, amount, "교환용 포인트 동결", exchangeRequestId);
            transaction.setPointAccount(this);
            this.transactions.add(transaction);
            addDomainEvent(new PointsFrozenEvent(userId, amount, exchangeRequestId));
        } else {
            throw new IllegalArgumentException("동결할 포인트가 부족합니다");
        }
    }

    public void unfreezePoints(PointAmount amount, String exchangeRequestId) {
        if (frozenBalance.isGreaterThan(amount) || frozenBalance.equals(amount)) {
            this.frozenBalance = this.frozenBalance.subtract(amount);
            this.balance = this.balance.add(amount);
            PointTransaction transaction = new PointTransaction(userId, PointTransactionType.UNFREEZE, amount, "교환 취소로 인한 포인트 해제", exchangeRequestId);
            transaction.setPointAccount(this);
            this.transactions.add(transaction);
            addDomainEvent(new PointsUnfrozenEvent(userId, amount, exchangeRequestId));
        } else {
            throw new IllegalArgumentException("해제할 동결 포인트가 부족합니다");
        }
    }

    public PointAmount getAvailableBalance() {
        return balance;
    }

    public PointAmount getTotalBalance() {
        return balance.add(frozenBalance);
    }
} 