package com.foodtraceability.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MaterialPurchaseDTO {
    private Long id;
    private Long materialId;
    private String materialName;
    private String batchNumber;
    private String supplierName;
    private String producerName;
    private String producerAddress;
    private LocalDateTime purchaseDate;
    private Double quantity;
    private String unit;
}
