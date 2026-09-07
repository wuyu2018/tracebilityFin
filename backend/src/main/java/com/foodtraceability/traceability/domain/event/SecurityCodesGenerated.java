package com.foodtraceability.traceability.domain.event;

import java.time.Instant;

public class SecurityCodesGenerated implements DomainEvent {
    private final Instant occurredAt;
    private final Long batchId;
    private final int quantity;

    public SecurityCodesGenerated(Long batchId, int quantity) {
        this.occurredAt = Instant.now();
        this.batchId = batchId;
        this.quantity = quantity;
    }

    @Override
    public Instant occurredAt() { return occurredAt; }

    public Long batchId() { return batchId; }

    public int quantity() { return quantity; }
}
