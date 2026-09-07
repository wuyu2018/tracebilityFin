package com.foodtraceability.anchor.repository;

import com.foodtraceability.anchor.entity.BlockchainAnchor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlockchainAnchorRepository extends JpaRepository<BlockchainAnchor, Long> {

    List<BlockchainAnchor> findByChainTypeAndBatchIdOrderByAnchorDateDesc(String chainType, Long batchId);

    @Query("SELECT MAX(a.anchorDate) FROM BlockchainAnchor a WHERE a.chainType = :chainType")
    Optional<LocalDate> findLatestAnchorDateByChainType(@Param("chainType") String chainType);
}
