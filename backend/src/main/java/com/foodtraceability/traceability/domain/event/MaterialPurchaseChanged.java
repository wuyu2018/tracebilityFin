package com.foodtraceability.traceability.domain.event;

import java.time.Instant;

public class MaterialPurchaseChanged implements DomainEvent {
    private final Instant occurredAt;
    private final Long purchaseId;
    private final String action;

    public MaterialPurchaseChanged(Long purchaseId, String action) {
        this.occurredAt = Instant.now();
        this.purchaseId = purchaseId;
        this.action = action;
    }

    @Override
    public Instant occurredAt() { return occurredAt; }

    public Long purchaseId() { return purchaseId; }

    public String action() { return action; }
}
