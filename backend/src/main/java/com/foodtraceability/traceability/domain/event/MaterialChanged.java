package com.foodtraceability.traceability.domain.event;

import java.time.Instant;

public class MaterialChanged implements DomainEvent {
    private final Instant occurredAt;
    private final Long materialId;
    private final String action;

    public MaterialChanged(Long materialId, String action) {
        this.occurredAt = Instant.now();
        this.materialId = materialId;
        this.action = action;
    }

    @Override
    public Instant occurredAt() { return occurredAt; }

    public Long materialId() { return materialId; }

    public String action() { return action; }
}
