package com.foodtraceability.repository;

import com.foodtraceability.entity.TransportSale;
import com.foodtraceability.entity.ProductionBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportSaleRepository extends JpaRepository<TransportSale, Long> {
    List<TransportSale> findByBatch(ProductionBatch batch);
}