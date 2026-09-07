package com.foodtraceability.dto;

import lombok.Data;

@Data
public class GenerateSecurityCodeRequest {
    private Long batchId;
    private Integer quantity;
}