package com.foodtraceability.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductionBatchDTO {
    private Long id;
    private String batchNumber;
    private Long productId;
    private String productName;
    private LocalDate productionDate;
    private String shelfLife;
    private Double quantity;
    private String unit;
    private List<Long> materialIds;
    private StorageDTO storage;
    private TransportSaleDTO transportSale;
    private LocalDateTime createdAt;
}