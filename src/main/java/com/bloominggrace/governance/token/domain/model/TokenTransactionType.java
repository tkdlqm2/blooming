package com.bloominggrace.governance.token.domain.model;

public enum TokenTransactionType {
    MINT("토큰 민팅"),
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