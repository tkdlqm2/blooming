package com.bloominggrace.governance.exchange.domain.model;

import com.bloominggrace.governance.exchange.domain.event.ExchangeCancelledEvent;
import com.bloominggrace.governance.exchange.domain.event.ExchangeCompletedEvent;
import com.bloominggrace.governance.exchange.domain.event.ExchangeFailedEvent;
import com.bloominggrace.governance.exchange.domain.event.ExchangeProcessingStartedEvent;
import com.bloominggrace.governance.point.domain.model.PointAmount;
import com.bloominggrace.governance.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "exchange_requests")
@Getter
@NoArgsConstructor
public class ExchangeRequest extends AggregateRoot {

    @EmbeddedId
    private ExchangeRequestId id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "point_amount", nullable = false))
    })
    private PointAmount pointAmount;

    @Column(name = "wallet_address", nullable = false)
    private String walletAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeStatus status;

    @Column(name = "transaction_signature")
    private String transactionSignature;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public ExchangeRequest(UUID userId, PointAmount pointAmount, String walletAddress) {
        this.id = ExchangeRequestId.generate();
        this.userId = userId;
        this.pointAmount = pointAmount;
        this.walletAddress = walletAddress;
        this.status = ExchangeStatus.REQUESTED;
        this.createdAt = LocalDateTime.now();
    }

    public void process() {
        if (this.status != ExchangeStatus.REQUESTED) {
            throw new IllegalStateException("처리할 수 없는 상태입니다: " + this.status);
        }
        this.status = ExchangeStatus.PROCESSING;
        addDomainEvent(new ExchangeProcessingStartedEvent(this.id, this.userId, this.pointAmount));
    }

    public void complete(String transactionSignature) {
        if (this.status != ExchangeStatus.PROCESSING) {
            throw new IllegalStateException("완료할 수 없는 상태입니다: " + this.status);
        }
        this.status = ExchangeStatus.COMPLETED;
        this.transactionSignature = transactionSignature;
        this.completedAt = LocalDateTime.now();
        addDomainEvent(new ExchangeCompletedEvent(this.id, this.userId, this.pointAmount, transactionSignature));
    }

    public void cancel() {
        if (this.status == ExchangeStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 교환은 취소할 수 없습니다");
        }
        this.status = ExchangeStatus.CANCELLED;
        addDomainEvent(new ExchangeCancelledEvent(this.id, this.userId, this.pointAmount));
    }

    public void fail() {
        this.status = ExchangeStatus.FAILED;
        addDomainEvent(new ExchangeFailedEvent(this.id, this.userId, this.pointAmount));
    }

    public ExchangeRequestId getId() {
        return id;
    }
} 