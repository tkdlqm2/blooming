package com.bloominggrace.governance.token.domain.model;

import com.bloominggrace.governance.shared.domain.ValueObject;

import java.math.BigDecimal;
import java.util.Objects;

public class TokenBalance extends ValueObject {
    private final BigDecimal totalBalance;
    private final BigDecimal stakedBalance;
    private final BigDecimal availableBalance;

    public TokenBalance(BigDecimal totalBalance, BigDecimal stakedBalance) {
        if (totalBalance == null || totalBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total balance must be non-negative");
        }
        if (stakedBalance == null || stakedBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Staked balance must be non-negative");
        }
        if (stakedBalance.compareTo(totalBalance) > 0) {
            throw new IllegalArgumentException("Staked balance cannot exceed total balance");
        }

        this.totalBalance = totalBalance;
        this.stakedBalance = stakedBalance;
        this.availableBalance = totalBalance.subtract(stakedBalance);
    }

    public TokenBalance(String totalBalance, String stakedBalance) {
        this(new BigDecimal(totalBalance), new BigDecimal(stakedBalance));
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

    public TokenBalance addTokens(TokenAmount amount) {
        return new TokenBalance(
            this.totalBalance.add(amount.getAmount()),
            this.stakedBalance
        );
    }
    
    public TokenBalance subtractTokens(TokenAmount amount) {
        if (amount.getAmount().compareTo(this.availableBalance) > 0) {
            throw new IllegalArgumentException("Insufficient available balance for subtraction");
        }
        return new TokenBalance(
            this.totalBalance.subtract(amount.getAmount()),
            this.stakedBalance
        );
    }

    public TokenBalance stakeTokens(TokenAmount amount) {
        if (amount.getAmount().compareTo(this.availableBalance) > 0) {
            throw new IllegalArgumentException("Insufficient available balance for staking");
        }
        return new TokenBalance(
            this.totalBalance,
            this.stakedBalance.add(amount.getAmount())
        );
    }

    public TokenBalance unstakeTokens(TokenAmount amount) {
        if (amount.getAmount().compareTo(this.stakedBalance) > 0) {
            throw new IllegalArgumentException("Insufficient staked balance for unstaking");
        }
        return new TokenBalance(
            this.totalBalance,
            this.stakedBalance.subtract(amount.getAmount())
        );
    }

    public boolean hasAvailableBalance(TokenAmount amount) {
        return this.availableBalance.compareTo(amount.getAmount()) >= 0;
    }

    public boolean hasStakedBalance(TokenAmount amount) {
        return this.stakedBalance.compareTo(amount.getAmount()) >= 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TokenBalance that = (TokenBalance) obj;
        return Objects.equals(totalBalance, that.totalBalance) &&
               Objects.equals(stakedBalance, that.stakedBalance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalBalance, stakedBalance);
    }

    @Override
    public String toString() {
        return String.format("TokenBalance{total=%s, staked=%s, available=%s}", 
                           totalBalance, stakedBalance, availableBalance);
    }
} 