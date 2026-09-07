package com.foodtraceability.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StorageDTO {
    private Long id;
    private Long batchId;
    private LocalDateTime storageTime;
    private LocalDateTime outboundTime;
    private Double quantity;
    private String unit;
    private String warehouseLocation;
}