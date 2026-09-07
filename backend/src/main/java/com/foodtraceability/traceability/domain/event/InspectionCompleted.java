package com.foodtraceability.traceability.domain.event;

import com.foodtraceability.traceability.domain.vo.InspectionResult;

import java.time.Instant;

public class InspectionCompleted implements DomainEvent {
    private final Instant occurredAt;
    private final Long inspectionId;
    private final Long batchId;
    private final InspectionResult result;

    public InspectionCompleted(Long inspectionId, Long batchId, InspectionResult result) {
        this.occurredAt = Instant.now();
        this.inspectionId = inspectionId;
        this.batchId = batchId;
        this.result = result;
    }

    @Override
    public Instant occurredAt() { return occurredAt; }

    public Long inspectionId() { return inspectionId; }
    public Long batchId() { return batchId; }
    public InspectionResult result() { return result; }
    public boolean isQualified() { return result.isQualified(); }
}
