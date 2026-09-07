package com.foodtraceability.traceability.application.dto;

public class CompleteInspectionRequest {
    private final Long batchId;
    private final String sampleName;
    private final Integer sampleQuantity;
    private final String sampleSpecification;
    private final String imageUrl;
    private final boolean qualified;
    private final String failReason;
    private final String inspectorName;

    public CompleteInspectionRequest(Long batchId, String sampleName, Integer sampleQuantity,
                                      String sampleSpecification, String imageUrl,
                                      boolean qualified, String failReason,
                                      String inspectorName) {
        this.batchId = batchId;
        this.sampleName = sampleName;
        this.sampleQuantity = sampleQuantity;
        this.sampleSpecification = sampleSpecification;
        this.imageUrl = imageUrl;
        this.qualified = qualified;
        this.failReason = failReason;
        this.inspectorName = inspectorName;
    }

    public Long batchId() { return batchId; }
    public String sampleName() { return sampleName; }
    public Integer sampleQuantity() { return sampleQuantity; }
    public String sampleSpecification() { return sampleSpecification; }
    public String imageUrl() { return imageUrl; }
    public boolean qualified() { return qualified; }
    public String failReason() { return failReason; }
    public String inspectorName() { return inspectorName; }
}
