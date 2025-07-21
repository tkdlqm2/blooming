package com.bloominggrace.governance.shared.domain;

import java.util.UUID;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EntityId entityId = (EntityId) obj;
        return value.equals(entityId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
} 