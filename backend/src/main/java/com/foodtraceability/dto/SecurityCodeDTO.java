package com.foodtraceability.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SecurityCodeDTO {
    private Long id;
    private String code;
    private Long batchId;
    private String batchNumber;
    private String status;
    private LocalDateTime firstScanTime;
    private Integer scanCount;
    private LocalDateTime createdAt;
}