package com.bloominggrace.governance.governance.domain.model;

public enum ProposalStatus {
    DRAFT("초안"),
    ACTIVE("활성"),
    VOTING("투표 중"),
    PASSED("통과"),
    REJECTED("거부"),
    EXPIRED("만료");

    private final String description;

    ProposalStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 