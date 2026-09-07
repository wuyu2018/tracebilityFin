package com.foodtraceability.repository;

import com.foodtraceability.entity.ProductionBatch;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionBatchRepository extends JpaRepository<ProductionBatch, Long> {
    @EntityGraph(attributePaths = {"product"})
    List<ProductionBatch> findByIsDeletedFalse();

    @EntityGraph(attributePaths = {"product"})
    List<ProductionBatch> findByProductIdAndIsDeletedFalse(Long productId);

    Optional<ProductionBatch> findByBatchNumberAndIsDeletedFalse(String batchNumber);

    boolean existsByProductId(Long productId);
}
