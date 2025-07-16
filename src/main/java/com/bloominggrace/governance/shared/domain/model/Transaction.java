package com.bloominggrace.governance.shared.domain.model;

import com.bloominggrace.governance.shared.domain.AggregateRoot;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 통합 트랜잭션 엔티티
 * 블록체인 트랜잭션과 RDB 트랜잭션을 모두 추적하기 위한 통합 모델
 */
@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor
public class Transaction extends AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "user_id"))
    })
    private UserId userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private BlockchainTransactionType transactionType;

    @Column(name = "transaction_hash", unique = true)
    private String transactionHash;

    @Column(name = "network_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NetworkType networkType;

    @Column(name = "amount", precision = 20, scale = 8)
    private BigDecimal amount;

    @Column(name = "from_address", nullable = false)
    private String fromAddress;

    @Column(name = "to_address")
    private String toAddress;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TransactionStatus {
        PENDING("대기 중"),
        CONFIRMED("확인됨"),
        FAILED("실패"),
        CANCELLED("취소됨");

        private final String description;

        TransactionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public Transaction(
            UserId userId,
            BlockchainTransactionType transactionType,
            NetworkType networkType,
            BigDecimal amount,
            String fromAddress,
            String toAddress,
            String description) {
        this.userId = userId;
        this.transactionType = transactionType;
        this.networkType = networkType;
        this.amount = amount;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.description = description;
        this.status = TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 트랜잭션 해시 설정 (블록체인에서 확인됨)
     */
    public void confirm(String transactionHash) {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is not in pending status");
        }
        this.transactionHash = transactionHash;
        this.status = TransactionStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 트랜잭션 실패 처리
     */
    public void fail(String reason) {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is not in pending status");
        }
        this.description = this.description + " - Failed: " + reason;
        this.status = TransactionStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 트랜잭션 취소 처리
     */
    public void cancel(String reason) {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is not in pending status");
        }
        this.description = this.description + " - Cancelled: " + reason;
        this.status = TransactionStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 