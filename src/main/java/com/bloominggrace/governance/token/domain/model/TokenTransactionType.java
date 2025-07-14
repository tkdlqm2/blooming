package com.bloominggrace.governance.token.domain.model;

public enum TokenTransactionType {
    MINT("토큰 민팅"),
    STAKE("토큰 스테이킹"),
    UNSTAKE("토큰 언스테이킹"),
    TRANSFER("토큰 전송"),
    BURN("토큰 소각");

    private final String description;

    TokenTransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 