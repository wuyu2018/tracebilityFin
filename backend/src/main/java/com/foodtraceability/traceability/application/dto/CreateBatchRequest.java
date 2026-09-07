package com.foodtraceability.traceability.application.dto;

import java.time.LocalDate;
import java.util.List;

public class CreateBatchRequest {
    private final Long productId;
    private final LocalDate productionDate;
    private final String shelfLife;
    private final Double quantity;
    private final String unit;
    private final List<Long> materialPurchaseIds;

    public CreateBatchRequest(Long productId, LocalDate productionDate, String shelfLife,
                              Double quantity, String unit, List<Long> materialPurchaseIds) {
        this.productId = productId;
        this.productionDate = productionDate;
        this.shelfLife = shelfLife;
        this.quantity = quantity;
        this.unit = unit;
        this.materialPurchaseIds = materialPurchaseIds;
    }

    public Long productId() { return productId; }
    public LocalDate productionDate() { return productionDate; }
    public String shelfLife() { return shelfLife; }
    public Double quantity() { return quantity; }
    public String unit() { return unit; }
    public List<Long> materialPurchaseIds() { return materialPurchaseIds; }
}
