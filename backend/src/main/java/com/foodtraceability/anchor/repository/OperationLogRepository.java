package com.foodtraceability.anchor.repository;

import com.foodtraceability.anchor.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    List<OperationLog> findByEntityTypeOrderByCreatedAtDesc(String entityType);
    List<OperationLog> findByOperatorOrderByCreatedAtDesc(String operator);
}
