package com.foodtraceability.traceability.interfaces.dto;

public class MaterialPurchaseResponse {
    private final Long id;
    private final Long materialId;
    private final String materialName;
    private final String batchNumber;
    private final String supplierName;
    private final String producerName;
    private final String producerAddress;
    private final String purchaseDate;
    private final Double quantity;
    private final String unit;
    private final boolean isDeleted;

    public MaterialPurchaseResponse(Long id, Long materialId, String materialName,
                                    String batchNumber, String supplierName,
                                    String producerName, String producerAddress,
                                    String purchaseDate, Double quantity,
                                    String unit, boolean isDeleted) {
        this.id = id;
        this.materialId = materialId;
        this.materialName = materialName;
        this.batchNumber = batchNumber;
        this.supplierName = supplierName;
        this.producerName = producerName;
        this.producerAddress = producerAddress;
        this.purchaseDate = purchaseDate;
        this.quantity = quantity;
        this.unit = unit;
        this.isDeleted = isDeleted;
    }

    public static MaterialPurchaseResponse from(
            com.foodtraceability.traceability.application.dto.MaterialPurchaseResponse r) {
        return new MaterialPurchaseResponse(
                r.id(), r.materialId(), r.materialName(), r.batchNumber(),
                r.supplierName(), r.producerName(), r.producerAddress(),
                r.purchaseDate() != null ? r.purchaseDate().toString() : null,
                r.quantity(), r.unit(), r.isDeleted());
    }

    public Long getId() { return id; }
    public Long getMaterialId() { return materialId; }
    public String getMaterialName() { return materialName; }
    public String getBatchNumber() { return batchNumber; }
    public String getSupplierName() { return supplierName; }
    public String getProducerName() { return producerName; }
    public String getProducerAddress() { return producerAddress; }
    public String getPurchaseDate() { return purchaseDate; }
    public Double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public boolean getIsDeleted() { return isDeleted; }
}
