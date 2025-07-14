package com.bloominggrace.governance.token.domain.model;

import com.bloominggrace.governance.shared.domain.ValueObject;
import com.bloominggrace.governance.shared.domain.UserId;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "token_transactions")
public class TokenTransaction extends ValueObject {
    
    @EmbeddedId
    private TokenTransactionId id;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "user_id"))
    })
    private UserId userId;
    
    @Column(name = "wallet_address", nullable = false)
    private String walletAddress;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TokenTransactionType transactionType;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "amount", precision = 20, scale = 8))
    })
    private TokenAmount amount;
    
    @Column(name = "transaction_signature")
    private String transactionSignature;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenTransactionStatus status;

    protected TokenTransaction() {}

    public TokenTransaction(
            UserId userId,
            String walletAddress,
            TokenTransactionType transactionType,
            TokenAmount amount,
            String description) {
        this.id = new TokenTransactionId();
        this.userId = userId;
        this.walletAddress = walletAddress;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.status = TokenTransactionStatus.PENDING;
    }

    public TokenTransactionId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public TokenTransactionType getTransactionType() {
        return transactionType;
    }

    public TokenAmount getAmount() {
        return amount;
    }

    public String getTransactionSignature() {
        return transactionSignature;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public TokenTransactionStatus getStatus() {
        return status;
    }

    public void confirm(String transactionSignature) {
        if (this.status != TokenTransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is not in pending status");
        }
        this.transactionSignature = transactionSignature;
        this.status = TokenTransactionStatus.CONFIRMED;
    }

    public void fail(String reason) {
        if (this.status != TokenTransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is not in pending status");
        }
        this.description = this.description + " - Failed: " + reason;
        this.status = TokenTransactionStatus.FAILED;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TokenTransaction that = (TokenTransaction) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("TokenTransaction{id=%s, userId=%s, type=%s, amount=%s, status=%s}",
                           id, userId, transactionType, amount, status);
    }
} 