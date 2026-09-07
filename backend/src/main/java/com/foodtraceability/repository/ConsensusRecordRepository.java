package com.foodtraceability.repository;

import com.foodtraceability.entity.ConsensusRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsensusRecordRepository extends JpaRepository<ConsensusRecord, Long> {

    Optional<ConsensusRecord> findBySequenceNumber(Long sequenceNumber);

    void deleteBySequenceNumber(Long sequenceNumber);
}
