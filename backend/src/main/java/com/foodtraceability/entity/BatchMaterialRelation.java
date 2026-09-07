package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "batch_material_relation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchMaterialRelation {

    @EmbeddedId
    private BatchMaterialRelationId id;

    public static BatchMaterialRelation create(Long batchId, Long materialPurchaseId) {
        BatchMaterialRelation relation = new BatchMaterialRelation();
        relation.id = new BatchMaterialRelationId(batchId, materialPurchaseId);
        return relation;
    }
}
