package com.foodtraceability.repository;

import com.foodtraceability.entity.BatchMaterialRelation;
import com.foodtraceability.entity.BatchMaterialRelationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchMaterialRelationRepository extends JpaRepository<BatchMaterialRelation, BatchMaterialRelationId> {
    List<BatchMaterialRelation> findById_BatchId(Long batchId);
}
