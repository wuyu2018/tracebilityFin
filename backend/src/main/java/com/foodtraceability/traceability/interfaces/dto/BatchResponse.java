package com.foodtraceability.traceability.interfaces.dto;

public class BatchResponse {
    private final Long id;
    private final String batchNumber;
    private final String productName;

    public BatchResponse(Long id, String batchNumber, String productName) {
        this.id = id;
        this.batchNumber = batchNumber;
        this.productName = productName;
    }

    public Long getId() { return id; }
    public String getBatchNumber() { return batchNumber; }
    public String getProductName() { return productName; }
}
