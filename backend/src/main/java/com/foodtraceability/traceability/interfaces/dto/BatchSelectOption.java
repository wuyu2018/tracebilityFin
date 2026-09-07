package com.foodtraceability.traceability.interfaces.dto;

public class BatchSelectOption {
    private Long id;
    private String batchNumber;
    private Long productId;
    private String productName;

    public BatchSelectOption(Long id, String batchNumber, Long productId, String productName) {
        this.id = id;
        this.batchNumber = batchNumber;
        this.productId = productId;
        this.productName = productName;
    }

    public Long getId() { return id; }
    public String getBatchNumber() { return batchNumber; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
}
