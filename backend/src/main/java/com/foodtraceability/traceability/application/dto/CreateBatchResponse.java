package com.foodtraceability.traceability.application.dto;

public class CreateBatchResponse {
    private final Long id;
    private final String batchNumber;
    private final String productName;

    public CreateBatchResponse(Long id, String batchNumber, String productName) {
        this.id = id;
        this.batchNumber = batchNumber;
        this.productName = productName;
    }

    public Long id() { return id; }
    public String batchNumber() { return batchNumber; }
    public String productName() { return productName; }
}
