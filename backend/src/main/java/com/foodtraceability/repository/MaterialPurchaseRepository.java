package com.foodtraceability.repository;

import com.foodtraceability.entity.MaterialPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialPurchaseRepository extends JpaRepository<MaterialPurchase, Long> {
    List<MaterialPurchase> findByIsDeletedFalse();
    List<MaterialPurchase> findByMaterialIdAndIsDeletedFalse(Long materialId);
    Optional<MaterialPurchase> findByBatchNumberAndIsDeletedFalse(String batchNumber);
}
