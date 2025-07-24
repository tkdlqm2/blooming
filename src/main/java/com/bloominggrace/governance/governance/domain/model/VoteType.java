package com.bloominggrace.governance.governance.domain.model;

public enum VoteType {
    YES("찬성"),
    NO("반대"),
    ABSTAIN("기권");

    private final String description;

    VoteType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 