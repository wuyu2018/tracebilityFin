package com.foodtraceability.traceability.interfaces.dto;

import java.time.LocalDateTime;

public class StorageResponse {
    private final Long id;
    private final Long batchId;
    private final LocalDateTime storageTime;
    private final LocalDateTime outboundTime;
    private final Double quantity;
    private final String unit;
    private final String warehouseLocation;

    public StorageResponse(Long id, Long batchId, LocalDateTime storageTime, LocalDateTime outboundTime,
                           Double quantity, String unit, String warehouseLocation) {
        this.id = id;
        this.batchId = batchId;
        this.storageTime = storageTime;
        this.outboundTime = outboundTime;
        this.quantity = quantity;
        this.unit = unit;
        this.warehouseLocation = warehouseLocation;
    }

    public Long getId() { return id; }
    public Long getBatchId() { return batchId; }
    public LocalDateTime getStorageTime() { return storageTime; }
    public LocalDateTime getOutboundTime() { return outboundTime; }
    public Double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getWarehouseLocation() { return warehouseLocation; }
}
