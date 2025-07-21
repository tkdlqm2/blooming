package com.bloominggrace.governance.shared.infrastructure.converter;

import com.bloominggrace.governance.shared.domain.UserId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

/**
 * UserId 값 객체를 JPA에서 올바르게 처리하기 위한 변환기
 */
@Converter
public class UserIdConverter implements AttributeConverter<UserId, UUID> {

    @Override
    public UUID convertToDatabaseColumn(UserId userId) {
        return userId != null ? userId.getValue() : null;
    }

    @Override
    public UserId convertToEntityAttribute(UUID uuid) {
        return uuid != null ? new UserId(uuid) : null;
    }
} 