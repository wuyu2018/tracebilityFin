package com.foodtraceability.service;

import com.foodtraceability.agent.service.AgentBlockchainService;
import com.foodtraceability.entity.BlockchainRetryTask;
import com.foodtraceability.repository.BlockchainRetryTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BlockchainRetryService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainRetryService.class);
    private static final int BATCH_SIZE = 10;

    private final BlockchainRetryTaskRepository retryTaskRepo;
    private final AgentBlockchainService agentBlockchainService;

    public BlockchainRetryService(BlockchainRetryTaskRepository retryTaskRepo,
                                   AgentBlockchainService agentBlockchainService) {
        this.retryTaskRepo = retryTaskRepo;
        this.agentBlockchainService = agentBlockchainService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleRetry(String chainType, String entityType, Long entityId,
                               String action, String rawData, Long batchId,
                               Long operatorId, String errorMessage) {
        BlockchainRetryTask task = new BlockchainRetryTask();
        task.setChainType(chainType);
        task.setEntityType(entityType);
        task.setEntityId(entityId);
        task.setAction(action);
        task.setRawData(rawData);
        task.setBatchId(batchId);
        task.setOperatorId(operatorId);
        task.setStatus(BlockchainRetryTask.RetryStatus.PENDING);
        task.setLastError(errorMessage);
        task.setNextRetryAt(LocalDateTime.now().plusMinutes(1));
        retryTaskRepo.save(task);
        log.info("[RetryTask] Scheduled retry #{} for {}/{} id={}",
                task.getId(), chainType, entityType, entityId);
    }

    @Scheduled(fixedDelay = 60000)
    public void processPendingTasks() {
        List<BlockchainRetryTask> tasks = retryTaskRepo.findByStatusAndNextRetryAtBefore(
                BlockchainRetryTask.RetryStatus.PENDING, LocalDateTime.now());

        if (tasks.isEmpty()) {
            return;
        }

        int count = Math.min(tasks.size(), BATCH_SIZE);
        List<BlockchainRetryTask> batch = tasks.subList(0, count);
        log.info("[RetryTask] Processing {} pending retry tasks (total: {})", count, tasks.size());

        for (BlockchainRetryTask task : batch) {
            processSingleTask(task);
        }
    }

    private void processSingleTask(BlockchainRetryTask task) {
        task.setStatus(BlockchainRetryTask.RetryStatus.PROCESSING);
        retryTaskRepo.save(task);

        try {
            agentBlockchainService.appendBlockWithConsensus(
                    task.getChainType(),
                    task.getEntityType(),
                    task.getEntityId(),
                    task.getAction(),
                    task.getRawData(),
                    task.getOperatorId());

            task.setStatus(BlockchainRetryTask.RetryStatus.SUCCESS);
            task.setLastError(null);
            retryTaskRepo.save(task);
            log.info("[RetryTask] Retry #{} SUCCESS for {}/{} id={} after {} attempts",
                    task.getId(), task.getChainType(), task.getEntityType(),
                    task.getEntityId(), task.getRetryCount() + 1);

        } catch (Exception e) {
            task.incrementRetry();
            task.setLastError(e.getMessage());

            if (task.getRetryCount() >= task.getMaxRetries()) {
                task.setStatus(BlockchainRetryTask.RetryStatus.FAILED);
                log.error("[RetryTask] Retry #{} FAILED permanently for {}/{} id={} after {} attempts",
                        task.getId(), task.getChainType(), task.getEntityType(),
                        task.getEntityId(), task.getRetryCount(), e);
            } else {
                task.setStatus(BlockchainRetryTask.RetryStatus.PENDING);
                log.warn("[RetryTask] Retry #{} attempt {} failed for {}/{} id={}, next retry in {}min",
                        task.getId(), task.getRetryCount(), task.getChainType(),
                        task.getEntityType(), task.getEntityId(),
                        (long) Math.pow(2, task.getRetryCount() - 1));
            }
            retryTaskRepo.save(task);
        }
    }

    public List<BlockchainRetryTask> getPendingTasks() {
        return retryTaskRepo.findByStatus(BlockchainRetryTask.RetryStatus.PENDING);
    }

    public List<BlockchainRetryTask> getFailedTasks() {
        return retryTaskRepo.findByStatus(BlockchainRetryTask.RetryStatus.FAILED);
    }

    public long getPendingCount() {
        return retryTaskRepo.countByStatus(BlockchainRetryTask.RetryStatus.PENDING);
    }

    public long getFailedCount() {
        return retryTaskRepo.countByStatus(BlockchainRetryTask.RetryStatus.FAILED);
    }

    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    public void cleanupOldTasks() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        retryTaskRepo.deleteByUpdatedAtBefore(threeDaysAgo);
        log.info("[RetryTask] Cleaned up tasks older than 3 days (before {})", threeDaysAgo);
    }
}
