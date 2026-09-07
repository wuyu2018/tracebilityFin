package com.foodtraceability.repository;

import com.foodtraceability.entity.TraceabilityLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraceabilityLinkRepository extends JpaRepository<TraceabilityLink, Long> {

    List<TraceabilityLink> findByBatchId(Long batchId);

    boolean existsByBatchIdAndEntityTypeAndEntityId(Long batchId, String entityType, Long entityId);
}
