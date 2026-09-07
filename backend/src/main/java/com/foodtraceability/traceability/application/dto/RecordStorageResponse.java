package com.foodtraceability.traceability.application.dto;

public class RecordStorageResponse {
    private final Long id;
    private final Long batchId;
    private final String warehouseLocation;

    public RecordStorageResponse(Long id, Long batchId, String warehouseLocation) {
        this.id = id;
        this.batchId = batchId;
        this.warehouseLocation = warehouseLocation;
    }

    public Long id() { return id; }
    public Long batchId() { return batchId; }
    public String warehouseLocation() { return warehouseLocation; }
}
