package com.foodtraceability.repository;

import com.foodtraceability.entity.BlockchainRetryTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BlockchainRetryTaskRepository extends JpaRepository<BlockchainRetryTask, Long> {

    List<BlockchainRetryTask> findByStatusAndNextRetryAtBefore(
            BlockchainRetryTask.RetryStatus status, LocalDateTime now);

    List<BlockchainRetryTask> findByStatus(BlockchainRetryTask.RetryStatus status);

    long countByStatus(BlockchainRetryTask.RetryStatus status);

    void deleteByUpdatedAtBefore(LocalDateTime dateTime);
}
