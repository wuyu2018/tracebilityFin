package com.foodtraceability.traceability.application.dto;

import java.time.LocalDateTime;

public class RecordStorageRequest {
    private final Long batchId;
    private final LocalDateTime storageTime;
    private final LocalDateTime outboundTime;
    private final Double quantity;
    private final String unit;
    private final String warehouseLocation;

    public RecordStorageRequest(Long batchId, LocalDateTime storageTime, LocalDateTime outboundTime,
                                 Double quantity, String unit, String warehouseLocation) {
        this.batchId = batchId;
        this.storageTime = storageTime;
        this.outboundTime = outboundTime;
        this.quantity = quantity;
        this.unit = unit;
        this.warehouseLocation = warehouseLocation;
    }

    public Long batchId() { return batchId; }
    public LocalDateTime storageTime() { return storageTime; }
    public LocalDateTime outboundTime() { return outboundTime; }
    public Double quantity() { return quantity; }
    public String unit() { return unit; }
    public String warehouseLocation() { return warehouseLocation; }
}
