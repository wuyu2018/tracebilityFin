package com.foodtraceability.traceability.application.dto;

public class CompleteInspectionResponse {
    private final Long id;
    private final Long batchId;
    private final String resultStatus;

    public CompleteInspectionResponse(Long id, Long batchId, String resultStatus) {
        this.id = id;
        this.batchId = batchId;
        this.resultStatus = resultStatus;
    }

    public Long id() { return id; }
    public Long batchId() { return batchId; }
    public String resultStatus() { return resultStatus; }
}
