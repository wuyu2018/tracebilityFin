package com.foodtraceability.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class BatchMaterialRelationId implements Serializable {
    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "material_purchase_id")
    private Long materialPurchaseId;
}
