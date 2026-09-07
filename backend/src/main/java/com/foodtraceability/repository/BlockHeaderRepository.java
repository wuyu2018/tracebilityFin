package com.foodtraceability.repository;

import com.foodtraceability.entity.BlockHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockHeaderRepository extends JpaRepository<BlockHeader, Long> {
    Optional<BlockHeader> findTopByChainTypeOrderByIdDesc(String chainType);
}
