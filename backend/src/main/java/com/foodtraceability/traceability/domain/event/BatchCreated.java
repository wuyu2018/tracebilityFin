package com.foodtraceability.traceability.domain.event;

import com.foodtraceability.traceability.domain.vo.BatchNumber;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class BatchCreated implements DomainEvent {
    private final Instant occurredAt;
    private final Long batchId;
    private final BatchNumber batchNumber;
    private final Long productId;
    private final List<Long> materialPurchaseIds;

    public BatchCreated(Long batchId, BatchNumber batchNumber, Long productId, List<Long> materialPurchaseIds) {
        this.occurredAt = Instant.now();
        this.batchId = batchId;
        this.batchNumber = batchNumber;
        this.productId = productId;
        this.materialPurchaseIds = materialPurchaseIds != null
                ? Collections.unmodifiableList(materialPurchaseIds)
                : Collections.emptyList();
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }

    public Long batchId() {
        return batchId;
    }

    public BatchNumber batchNumber() {
        return batchNumber;
    }

    public Long productId() {
        return productId;
    }

    public List<Long> materialPurchaseIds() {
        return materialPurchaseIds;
    }

}
