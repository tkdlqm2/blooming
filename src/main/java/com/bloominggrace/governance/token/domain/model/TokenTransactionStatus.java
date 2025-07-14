package com.bloominggrace.governance.token.domain.model;

public enum TokenTransactionStatus {
    PENDING("대기 중"),
    CONFIRMED("확인됨"),
    FAILED("실패"),
    CANCELLED("취소됨");

    private final String description;

    TokenTransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 