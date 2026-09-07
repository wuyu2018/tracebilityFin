package com.foodtraceability.traceability.interfaces.dto;

import java.time.LocalDateTime;

public class UpdateMaterialPurchaseRequest {
    private String batchNumber;
    private String supplierName;
    private String producerName;
    private String producerAddress;
    private LocalDateTime purchaseDate;
    private Double quantity;
    private String unit;

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getProducerName() { return producerName; }
    public void setProducerName(String producerName) { this.producerName = producerName; }
    public String getProducerAddress() { return producerAddress; }
    public void setProducerAddress(String producerAddress) { this.producerAddress = producerAddress; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public com.foodtraceability.traceability.application.dto.UpdateMaterialPurchaseRequest toAppRequest() {
        return new com.foodtraceability.traceability.application.dto.UpdateMaterialPurchaseRequest(
                batchNumber, supplierName, producerName, producerAddress,
                purchaseDate, quantity, unit);
    }
}
