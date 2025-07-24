package com.bloominggrace.governance.token.domain.model;

import com.bloominggrace.governance.shared.domain.AggregateRoot;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.security.infrastructure.converter.UserIdConverter;

import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import lombok.Getter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "token_accounts")
@Getter
public class TokenAccount extends AggregateRoot {
    
    @Id
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;
    
    @Convert(converter = UserIdConverter.class)
    @Column(name = "user_id", nullable = false)
    private UserId userId;
    
    @Column(name = "total_balance", precision = 38, scale = 18, nullable = false)
    private BigDecimal totalBalance = BigDecimal.ZERO;
    
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
        this.availableBalance = BigDecimal.ZERO;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }



    public void mintTokens(BigDecimal amount, String description) {
        if (!isActive) {
            throw new IllegalStateException("Token account is not active");
        }
        
        this.totalBalance = this.totalBalance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.updatedAt = LocalDateTime.now();
        

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
        

    }
    
    public void receiveTokens(BigDecimal amount, String description) {
        if (!isActive) {
            throw new IllegalStateException("Token account is not active");
        }
        
        this.totalBalance = this.totalBalance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.updatedAt = LocalDateTime.now();
        

    }

    public void deactivate() {
        if (!isActive) {
            throw new IllegalStateException("Token account is already deactivated");
        }
        
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
        

    }

    public void activate() {
        if (isActive) {
            throw new IllegalStateException("Token account is already active");
        }
        
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
        

    }

    public boolean hasAvailableBalance(BigDecimal amount) {
        return this.availableBalance.compareTo(amount) >= 0;
    }

    @Override
    public String toString() {
        return String.format("TokenAccount{id=%s, walletId=%s, userId=%s, network=%s, contract=%s, symbol=%s, totalBalance=%s, availableBalance=%s, active=%s}",
                           id, wallet != null ? wallet.getId() : null, userId, network, contract, symbol, totalBalance, availableBalance, isActive);
    }
} 