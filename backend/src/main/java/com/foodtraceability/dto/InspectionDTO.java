package com.foodtraceability.dto;

import lombok.Data;

@Data
public class InspectionDTO {
    private Long id;
    private Long batchId;
    private String sampleName;
    private Integer sampleQuantity;
    private String sampleSpecification;
    private String imageUrl;
    private String inspectorName;
    private String inspectionTime;
}