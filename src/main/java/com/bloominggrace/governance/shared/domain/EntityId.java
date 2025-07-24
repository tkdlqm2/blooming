package com.bloominggrace.governance.shared.domain;

import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.UUID;

@MappedSuperclass
@EqualsAndHashCode
@ToString
public abstract class EntityId {
    protected UUID value;

    // JPA 엔티티를 위한 기본 생성자
    protected EntityId() {
        this.value = null;
    }

    protected EntityId(UUID value) {
        this.value = value;
    }

    public UUID getValue() {
        return value;
    }
} 