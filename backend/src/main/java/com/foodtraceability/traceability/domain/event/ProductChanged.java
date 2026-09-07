package com.foodtraceability.traceability.domain.event;

import java.time.Instant;

public class ProductChanged implements DomainEvent {
    private final Instant occurredAt;
    private final Long productId;
    private final String action;

    public ProductChanged(Long productId, String action) {
        this.occurredAt = Instant.now();
        this.productId = productId;
        this.action = action;
    }

    @Override
    public Instant occurredAt() { return occurredAt; }

    public Long productId() { return productId; }

    public String action() { return action; }
}
