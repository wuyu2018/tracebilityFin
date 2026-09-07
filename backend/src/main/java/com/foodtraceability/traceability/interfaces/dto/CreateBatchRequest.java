package com.foodtraceability.traceability.interfaces.dto;

import java.time.LocalDate;
import java.util.List;

public class CreateBatchRequest {
    private Long productId;
    private LocalDate productionDate;
    private String shelfLife;
    private Double quantity;
    private String unit;
    private List<Long> materialPurchaseIds;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }
    public String getShelfLife() { return shelfLife; }
    public void setShelfLife(String shelfLife) { this.shelfLife = shelfLife; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public List<Long> getMaterialPurchaseIds() { return materialPurchaseIds; }
    public void setMaterialPurchaseIds(List<Long> materialPurchaseIds) { this.materialPurchaseIds = materialPurchaseIds; }

    public com.foodtraceability.traceability.application.dto.CreateBatchRequest toAppRequest() {
        return new com.foodtraceability.traceability.application.dto.CreateBatchRequest(
                productId, productionDate, shelfLife, quantity, unit, materialPurchaseIds);
    }
}
