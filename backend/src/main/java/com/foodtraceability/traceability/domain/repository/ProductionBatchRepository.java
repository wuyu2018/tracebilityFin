package com.foodtraceability.traceability.domain.repository;

import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.traceability.domain.vo.BatchNumber;

import java.util.Optional;

public interface ProductionBatchRepository {
    ProductionBatch save(ProductionBatch batch);
    Optional<ProductionBatch> findById(Long id);
    Optional<ProductionBatch> findByBatchNumber(BatchNumber batchNumber);
    boolean existsByProductId(Long productId);
}
