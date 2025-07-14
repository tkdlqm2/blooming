package com.bloominggrace.governance.token.domain.model;

import com.bloominggrace.governance.shared.domain.AggregateRoot;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.event.TokenAccountActivatedEvent;
import com.bloominggrace.governance.token.domain.event.TokenAccountDeactivatedEvent;
import com.bloominggrace.governance.token.domain.event.TokensMintedEvent;
import com.bloominggrace.governance.token.domain.event.TokensStakedEvent;
import com.bloominggrace.governance.token.domain.event.TokensUnstakedEvent;
import com.bloominggrace.governance.token.domain.event.TokensTransferredEvent;
import com.bloominggrace.governance.token.domain.event.TokensBurnedEvent;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "token_accounts")
public class TokenAccount extends AggregateRoot {
    
    @EmbeddedId
    private TokenAccountId id;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "user_id"))
    })
    private UserId userId;
    
    @Column(name = "wallet_address", nullable = false, unique = true)
    private String walletAddress;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "totalBalance", column = @Column(name = "total_balance", precision = 20, scale = 8)),
        @AttributeOverride(name = "stakedBalance", column = @Column(name = "staked_balance", precision = 20, scale = 8))
    })
    private TokenBalance balance;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected TokenAccount() {}

    public TokenAccount(UserId userId, String walletAddress) {
        this.id = new TokenAccountId();
        this.userId = userId;
        this.walletAddress = walletAddress;
        this.balance = new TokenBalance("0", "0");
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public TokenAccountId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public TokenBalance getBalance() {
        return balance;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void mintTokens(TokenAmount amount, String description) {
        if (!isActive) {
            throw new IllegalStateException("Token account is not active");
        }
        
        TokenBalance newBalance = balance.addTokens(amount);
        this.balance = newBalance;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TokensMintedEvent(userId, amount, description));
    }

    public void stakeTokens(TokenAmount amount) {
        if (!isActive) {
            throw new IllegalStateException("Token account is not active");
        }
        
        if (!balance.hasAvailableBalance(amount)) {
            throw new IllegalArgumentException("Insufficient available balance for staking");
        }
        
        TokenBalance newBalance = balance.stakeTokens(amount);
        this.balance = newBalance;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TokensStakedEvent(userId, amount));
    }

    public void unstakeTokens(TokenAmount amount) {
        if (!isActive) {
            throw new IllegalStateException("Token account is not active");
        }
        
        if (!balance.hasStakedBalance(amount)) {
            throw new IllegalArgumentException("Insufficient staked balance for unstaking");
        }
        
        TokenBalance newBalance = balance.unstakeTokens(amount);
        this.balance = newBalance;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TokensUnstakedEvent(userId, amount));
    }
    
    public void transferTokens(TokenAmount amount, String description) {
        if (!isActive) {
            throw new IllegalStateException("Token account is not active");
        }
        
        if (!balance.hasAvailableBalance(amount)) {
            throw new IllegalArgumentException("Insufficient available balance for transfer");
        }
        
        TokenBalance newBalance = balance.subtractTokens(amount);
        this.balance = newBalance;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TokensTransferredEvent(userId, null, amount, description));
    }
    
    public void burnTokens(TokenAmount amount, String description) {
        if (!isActive) {
            throw new IllegalStateException("Token account is not active");
        }
        
        if (!balance.hasAvailableBalance(amount)) {
            throw new IllegalArgumentException("Insufficient available balance for burning");
        }
        
        TokenBalance newBalance = balance.subtractTokens(amount);
        this.balance = newBalance;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TokensBurnedEvent(userId, amount, description));
    }

    public void deactivate() {
        if (!isActive) {
            throw new IllegalStateException("Token account is already deactivated");
        }
        
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TokenAccountDeactivatedEvent(userId, walletAddress));
    }

    public void activate() {
        if (isActive) {
            throw new IllegalStateException("Token account is already active");
        }
        
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TokenAccountActivatedEvent(userId, walletAddress));
    }

    public boolean hasAvailableBalance(TokenAmount amount) {
        return balance.hasAvailableBalance(amount);
    }

    public boolean hasStakedBalance(TokenAmount amount) {
        return balance.hasStakedBalance(amount);
    }

    @Override
    public String toString() {
        return String.format("TokenAccount{id=%s, userId=%s, walletAddress=%s, balance=%s, active=%s}",
                           id, userId, walletAddress, balance, isActive);
    }
} 