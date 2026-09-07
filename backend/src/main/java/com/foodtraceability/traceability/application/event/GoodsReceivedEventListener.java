package com.foodtraceability.traceability.application.event;

import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.foodtraceability.agent.service.AgentBlockchainService;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.repository.StorageRepository;
import com.foodtraceability.service.BlockchainRetryService;
import com.foodtraceability.traceability.domain.event.GoodsReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GoodsReceivedEventListener {

    private static final Logger log = LoggerFactory.getLogger(GoodsReceivedEventListener.class);

    private final ProductionBatchRepository batchRepo;
    private final StorageRepository storageRepo;
    private final MultiAgentCoordinator agentCoordinator;
    private final AgentBlockchainService agentBlockchainService;
    private final BlockchainRetryService blockchainRetryService;

    public GoodsReceivedEventListener(ProductionBatchRepository batchRepo,
                                       StorageRepository storageRepo,
                                       MultiAgentCoordinator agentCoordinator,
                                       AgentBlockchainService agentBlockchainService,
                                       BlockchainRetryService blockchainRetryService) {
        this.batchRepo = batchRepo;
        this.storageRepo = storageRepo;
        this.agentCoordinator = agentCoordinator;
        this.agentBlockchainService = agentBlockchainService;
        this.blockchainRetryService = blockchainRetryService;
    }
    // “收货入库”事件处理器：当生产批次完成生产并入库时，更新批次的storageId，并通过智能合约记录入库信息到区块链，同时通知流通代理进行后续处理。
    @TransactionalEventListener
    public void onGoodsReceived(GoodsReceived event) {
        log.info("[Event] GoodsReceived: storageId={}, batchId={}", event.storageId(), event.batchId());
        batchRepo.findById(event.batchId()).ifPresent(batch -> {
            batch.setStorageId(event.storageId());
            batchRepo.save(batch);
            log.debug("[Event] ProductionBatch.storageId updated: batchId={}, storageId={}",
                    event.batchId(), event.storageId());
        });

        storageRepo.findById(event.storageId()).ifPresent(storage -> {
            String snapshot;
            try {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("storageId", storage.getId());
                data.put("batchId", storage.getBatchId());
                data.put("storageTime", storage.getStorageTime() != null ? storage.getStorageTime().toString() : null);
                data.put("outboundTime", storage.getOutboundTime() != null ? storage.getOutboundTime().toString() : null);
                data.put("quantity", storage.getQuantity() != null ? storage.getQuantity() : 0.0);
                data.put("unit", storage.getUnit() != null ? storage.getUnit() : "");
                data.put("warehouseLocation", storage.getWarehouseLocation() != null ? storage.getWarehouseLocation() : "");
                snapshot = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data);
            } catch (Exception e) {
                log.error("[Blockchain] Failed to build snapshot for Storage id={}", storage.getId(), e);
                return;
            }
            try {
                agentBlockchainService.appendBlockWithConsensus(
                        "BATCH", "STORAGE", storage.getId(), "CREATE",
                        snapshot, null);
                log.info("[Blockchain] Storage block appended via agent for batchId={}, storageId={}",
                        storage.getBatchId(), storage.getId());
            } catch (Exception e) {
                log.error("[Blockchain] Failed to append block for batchId={}, storageId={} — scheduling retry",
                        storage.getBatchId(), storage.getId(), e);
                blockchainRetryService.scheduleRetry(
                        "BATCH", "STORAGE", storage.getId(), "CREATE",
                        snapshot, null, null, e.getMessage());
            }
        });

        try {
            var circulationAgent = agentCoordinator.getCirculationAgent();
            if (circulationAgent.isAuthorized()) {
                circulationAgent.recordStorage(event.batchId().toString(), event.storageId().toString());
                circulationAgent.updateCreditForTimeliness(true);
                log.info("[Agent] CirculationAgent updated for storage batchId={}", event.batchId());
            }
        } catch (Exception e) {
            log.error("[Agent] Failed to notify CirculationAgent", e);
        }
    }
}
