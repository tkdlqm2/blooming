package com.bloominggrace.governance.point.domain.model;

import com.bloominggrace.governance.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "point_transactions")
@Getter
@NoArgsConstructor
public class PointTransaction extends AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointTransactionType type;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String reason;

    @Column(name = "exchange_request_id")
    private String exchangeRequestId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PointTransaction(UUID userId, PointTransactionType type, PointAmount amount, String reason) {
        this.userId = userId;
        this.type = type;
        this.amount = amount.getAmount();
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }

    public PointTransaction(UUID userId, PointTransactionType type, PointAmount amount, String reason, String exchangeRequestId) {
        this(userId, type, amount, reason);
        this.exchangeRequestId = exchangeRequestId;
    }
} 