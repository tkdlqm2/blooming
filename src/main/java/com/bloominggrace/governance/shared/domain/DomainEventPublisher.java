package com.bloominggrace.governance.shared.domain;

import java.util.List;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
    void publishAll(List<DomainEvent> events);
} 