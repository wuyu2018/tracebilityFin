package com.foodtraceability.traceability.domain.event;

import java.time.Instant;

public class BatchSoftDeleted implements DomainEvent {
    private final Instant occurredAt;
    private final Long batchId;

    public BatchSoftDeleted(Long batchId) {
        this.occurredAt = Instant.now();
        this.batchId = batchId;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }

    public Long batchId() {
        return batchId;
    }
}
