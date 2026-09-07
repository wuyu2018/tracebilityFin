package com.foodtraceability.traceability.interfaces.dto;

public class CompleteInspectionResponse {
    private final Long id;
    private final Long batchId;
    private final String resultStatus;

    public CompleteInspectionResponse(Long id, Long batchId, String resultStatus) {
        this.id = id;
        this.batchId = batchId;
        this.resultStatus = resultStatus;
    }

    public Long getId() { return id; }
    public Long getBatchId() { return batchId; }
    public String getResultStatus() { return resultStatus; }
}
