package com.foodtraceability.repository;

import com.foodtraceability.entity.SecurityCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityCodeRepository extends JpaRepository<SecurityCode, Long> {
    Optional<SecurityCode> findByCode(String code);
    List<SecurityCode> findByBatch_Id(Long batchId);
    long countByBatch_Id(Long batchId);
    boolean existsByBatch_Product_Id(Long productId);
}
