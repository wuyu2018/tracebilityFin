package com.foodtraceability.traceability.interfaces.dto;

import java.time.LocalDateTime;

public class RecordStorageRequest {
    private Long batchId;
    private LocalDateTime storageTime;
    private LocalDateTime outboundTime;
    private Double quantity;
    private String unit;
    private String warehouseLocation;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public LocalDateTime getStorageTime() { return storageTime; }
    public void setStorageTime(LocalDateTime storageTime) { this.storageTime = storageTime; }
    public LocalDateTime getOutboundTime() { return outboundTime; }
    public void setOutboundTime(LocalDateTime outboundTime) { this.outboundTime = outboundTime; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getWarehouseLocation() { return warehouseLocation; }
    public void setWarehouseLocation(String warehouseLocation) { this.warehouseLocation = warehouseLocation; }

    public com.foodtraceability.traceability.application.dto.RecordStorageRequest toAppRequest() {
        return new com.foodtraceability.traceability.application.dto.RecordStorageRequest(
                batchId, storageTime, outboundTime, quantity, unit, warehouseLocation);
    }
}
