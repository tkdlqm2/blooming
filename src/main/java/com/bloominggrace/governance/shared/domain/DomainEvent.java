package com.bloominggrace.governance.shared.domain;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime getOccurredOn();
} 