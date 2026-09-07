package com.foodtraceability.repository;

import com.foodtraceability.entity.Inspection;
import com.foodtraceability.entity.ProductionBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    List<Inspection> findByBatch(ProductionBatch batch);

    List<Inspection> findByBatch_Id(Long batchId);
}