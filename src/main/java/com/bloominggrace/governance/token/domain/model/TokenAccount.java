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
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "token_accounts")
public class TokenAccount extends AggregateRoot {
    
    @Id
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "user_id"))
    })
    private UserId userId;
    
    @Column(name = "total_balance", precision = 38, scale = 18, nullable = false)
    private BigDecimal totalBalance = BigDecimal.ZERO;
    
    @Column(name = "staked_balance", precision = 38, scale = 18, nullable = false)
    private BigDecimal stakedBalance = BigDecimal.ZERO;
    
    @Column(name = "available_balance", precision = 38, scale = 18, nullable = false)
    private BigDecimal availableBalance = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "network", nullable = false)
    private NetworkType network;
    
    @Column(name = "contract", nullable = false)
    private String contract;
    
    @Column(name = "symbol", nullable = false)
    private String symbol;
    
    @Column(name = "wallet_address", nullable = false)
    private String walletAddress;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected TokenAccount() {}

    public TokenAccount(Wallet wallet, UserId userId, NetworkType network, String contract, String symbol) {
        this.id = UUID.randomUUID();
        this.wallet = wallet;
        this.userId = userId;
        this.network = network;
        this.contract = contract;
        this.symbol = symbol;
        this.walletAddress = wallet.getWalletAddress();
        this.totalBalance = BigDecimal.ZERO;
        this.stakedBalance = BigDecimal.ZERO;
        this.availableBalance = BigDecimal.ZERO;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public UserId getUserId() {
        return userId;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public BigDecimal getStakedBalance() {
        return stakedBalance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public NetworkType getNetwork() {
        return network;
    }

    public String getContract() {
        return contract;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getWalletAddress() {
        return walletAddress;
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

    public void mintTokens(BigDecimal amount, String description) {
        if (!isActive) {
            throw new IllegalStateException("Token account is not active");
        }
        
        this.totalBalance = this.totalBalance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TokensMintedEvent(userId, new TokenAmount(amount.toString()), description));
    }

    public void stakeTokens(BigDecimal amount) {
        if (!isActive) {
            throw new IllegalStateException("Token account is not active");
        }
        
        if (this.availableBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient available balance for staking");
        }
        
        this.availableBalance = this.availableBalance.subtract(amount);
        this.stakedBalance = this.stakedBalance.add(amount);
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TokensStakedEvent(userId, new TokenAmount(amount.toString())));
    }

    public void unstakeTokens(BigDecimal amount) {
        if (!isActive) {
            throw new IllegalStateException("Token account is not active");
        }
        
        if (this.stakedBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient staked balance for unstaking");
        }
        
        this.stakedBalance = this.stakedBalance.subtract(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TokensUnstakedEvent(userId, new TokenAmount(amount.toString())));
    }
    
    public void transferTokens(BigDecimal amount, String description) {
        if (!isActive) {
            throw new IllegalStateException("Token account is not active");
        }
        
        if (this.availableBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient available balance for transfer");
        }
        
        this.availableBalance = this.availableBalance.subtract(amount);
        this.totalBalance = this.totalBalance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TokensTransferredEvent(userId, null, new TokenAmount(amount.toString()), description));
    }
    
    public void burnTokens(BigDecimal amount, String description) {
        if (!isActive) {
            throw new IllegalStateException("Token account is not active");
        }
        
        if (this.availableBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient available balance for burning");
        }
        
        this.availableBalance = this.availableBalance.subtract(amount);
        this.totalBalance = this.totalBalance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TokensBurnedEvent(userId, new TokenAmount(amount.toString()), description));
    }
    
    public void receiveTokens(BigDecimal amount, String description) {
        if (!isActive) {
            throw new IllegalStateException("Token account is not active");
        }
        
        this.totalBalance = this.totalBalance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TokensTransferredEvent(null, wallet.getWalletAddress(), new TokenAmount(amount.toString()), description));
    }

    public void deactivate() {
        if (!isActive) {
            throw new IllegalStateException("Token account is already deactivated");
        }
        
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TokenAccountDeactivatedEvent(userId, wallet.getWalletAddress()));
    }

    public void activate() {
        if (isActive) {
            throw new IllegalStateException("Token account is already active");
        }
        
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TokenAccountActivatedEvent(userId, wallet.getWalletAddress()));
    }

    public boolean hasAvailableBalance(BigDecimal amount) {
        return this.availableBalance.compareTo(amount) >= 0;
    }

    public boolean hasStakedBalance(BigDecimal amount) {
        return this.stakedBalance.compareTo(amount) >= 0;
    }

    @Override
    public String toString() {
        return String.format("TokenAccount{id=%s, walletId=%s, userId=%s, network=%s, contract=%s, symbol=%s, totalBalance=%s, stakedBalance=%s, availableBalance=%s, active=%s}",
                           id, wallet != null ? wallet.getId() : null, userId, network, contract, symbol, totalBalance, stakedBalance, availableBalance, isActive);
    }
} 