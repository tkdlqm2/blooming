package com.bloominggrace.governance.shared.domain;

import java.util.UUID;

public class UserId extends EntityId {
    
    // JPA 엔티티를 위한 기본 생성자
    protected UserId() {
        super();
    }
    
    public UserId(UUID value) {
        super(value);
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }
} 