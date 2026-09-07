package com.foodtraceability.traceability.interfaces.dto;

public class CompleteInspectionRequest {
    private Long batchId;
    private String sampleName;
    private Integer sampleQuantity;
    private String sampleSpecification;
    private String imageUrl;
    private Boolean qualified;
    private String failReason;
    private String inspectorName;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getSampleName() { return sampleName; }
    public void setSampleName(String sampleName) { this.sampleName = sampleName; }
    public Integer getSampleQuantity() { return sampleQuantity; }
    public void setSampleQuantity(Integer sampleQuantity) { this.sampleQuantity = sampleQuantity; }
    public String getSampleSpecification() { return sampleSpecification; }
    public void setSampleSpecification(String sampleSpecification) { this.sampleSpecification = sampleSpecification; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Boolean getQualified() { return qualified; }
    public void setQualified(Boolean qualified) { this.qualified = qualified; }
    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }
    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }

    public com.foodtraceability.traceability.application.dto.CompleteInspectionRequest toAppRequest() {
        return new com.foodtraceability.traceability.application.dto.CompleteInspectionRequest(
                batchId, sampleName, sampleQuantity, sampleSpecification, imageUrl,
                qualified != null && qualified, failReason, inspectorName);
    }
}
