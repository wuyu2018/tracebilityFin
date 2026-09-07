package com.foodtraceability.repository;

import com.foodtraceability.entity.BlockchainLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockchainLogRepository extends JpaRepository<BlockchainLog, Long> {

    List<BlockchainLog> findByChainTypeOrderByTimestampAsc(String chainType);

    List<BlockchainLog> findByChainTypeAndBatchIdOrderByTimestampAsc(String chainType, Long batchId);

    Optional<BlockchainLog> findTopByChainTypeOrderByTimestampDesc(String chainType);

    Optional<BlockchainLog> findTopByChainTypeAndBatchIdOrderByTimestampDesc(String chainType, Long batchId);

    long countByChainType(String chainType);

    boolean existsByChainType(String chainType);

    boolean existsByChainTypeAndEntityTypeAndEntityIdAndAction(String chainType, String entityType, Long entityId, String action);
}
