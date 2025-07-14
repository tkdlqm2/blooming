package com.bloominggrace.governance.token.application.dto;

import com.bloominggrace.governance.token.domain.model.TokenBalance;

import java.math.BigDecimal;

public class TokenBalanceDto {
    private final BigDecimal totalBalance;
    private final BigDecimal stakedBalance;
    private final BigDecimal availableBalance;

    public TokenBalanceDto(BigDecimal totalBalance, BigDecimal stakedBalance, BigDecimal availableBalance) {
        this.totalBalance = totalBalance;
        this.stakedBalance = stakedBalance;
        this.availableBalance = availableBalance;
    }

    public static TokenBalanceDto from(TokenBalance tokenBalance) {
        return new TokenBalanceDto(
            tokenBalance.getTotalBalance(),
            tokenBalance.getStakedBalance(),
            tokenBalance.getAvailableBalance()
        );
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
} 