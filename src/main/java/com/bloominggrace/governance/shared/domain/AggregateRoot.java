package com.bloominggrace.governance.shared.domain;

import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class AggregateRoot {
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }


}