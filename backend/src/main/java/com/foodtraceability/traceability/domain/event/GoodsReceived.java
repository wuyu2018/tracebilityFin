package com.foodtraceability.traceability.domain.event;

import java.time.Instant;
import java.time.LocalDateTime;

public class GoodsReceived implements DomainEvent {
    private final Instant occurredAt;
    private final Long storageId;
    private final Long batchId;
    private final LocalDateTime storageTime;

    public GoodsReceived(Long storageId, Long batchId, LocalDateTime storageTime) {
        this.occurredAt = Instant.now();
        this.storageId = storageId;
        this.batchId = batchId;
        this.storageTime = storageTime;
    }

    @Override
    public Instant occurredAt() { return occurredAt; }

    public Long storageId() { return storageId; }
    public Long batchId() { return batchId; }
    public LocalDateTime storageTime() { return storageTime; }
}
