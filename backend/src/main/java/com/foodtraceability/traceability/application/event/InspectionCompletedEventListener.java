package com.foodtraceability.traceability.application.event;

import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.foodtraceability.agent.service.AgentBlockchainService;
import com.foodtraceability.entity.SecurityCode;
import com.foodtraceability.repository.InspectionRepository;
import com.foodtraceability.repository.SecurityCodeRepository;
import com.foodtraceability.service.BlockchainRetryService;
import com.foodtraceability.traceability.domain.event.InspectionCompleted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class InspectionCompletedEventListener {

    private static final Logger log = LoggerFactory.getLogger(InspectionCompletedEventListener.class);

    private final SecurityCodeRepository securityCodeRepo;
    private final InspectionRepository inspectionRepo;
    private final MultiAgentCoordinator agentCoordinator;
    private final AgentBlockchainService agentBlockchainService;
    private final BlockchainRetryService blockchainRetryService;

    public InspectionCompletedEventListener(SecurityCodeRepository securityCodeRepo,
                                             InspectionRepository inspectionRepo,
                                             MultiAgentCoordinator agentCoordinator,
                                             AgentBlockchainService agentBlockchainService,
                                             BlockchainRetryService blockchainRetryService) {
        this.securityCodeRepo = securityCodeRepo;
        this.inspectionRepo = inspectionRepo;
        this.agentCoordinator = agentCoordinator;
        this.agentBlockchainService = agentBlockchainService;
        this.blockchainRetryService = blockchainRetryService;
    }
    // “质检完成”事件处理器：当生产批次的质检完成后，根据质检结果更新批次的状态，如果质检不合格则冻结相关防伪码，并通过智能合约记录质检信息到区块链，同时通知生产代理进行信用评分调整。
    @TransactionalEventListener
    public void onInspectionCompleted(InspectionCompleted event) {
        log.info("[Event] InspectionCompleted: inspectionId={}, batchId={}, qualified={}",
                event.inspectionId(), event.batchId(), event.isQualified());

        if (!event.isQualified()) {
            freezeSecurityCodes(event.batchId());
        }

        inspectionRepo.findById(event.inspectionId()).ifPresent(inspection -> {
            String snapshot;
            try {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("inspectionId", inspection.getId());
                data.put("batchId", inspection.getBatchId());
                data.put("sampleName", inspection.getSampleName());
                data.put("sampleQuantity", inspection.getSampleQuantity());
                data.put("sampleSpecification", inspection.getSampleSpecification());
                data.put("resultStatus", inspection.getResultStatus());
                data.put("resultDetail", inspection.getResultDetail());
                data.put("inspectorName", inspection.getInspectorName());
                data.put("inspectionTime", inspection.getInspectionTime() != null
                        ? inspection.getInspectionTime().toString() : null);
                snapshot = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data);
            } catch (Exception e) {
                log.error("[Blockchain] Failed to build snapshot for Inspection id={}", inspection.getId(), e);
                return;
            }
            try {
                agentBlockchainService.appendBlockWithConsensus(
                        "BATCH", "INSPECTION", inspection.getId(), "CREATE",
                        snapshot, null);
                log.info("[Blockchain] Inspection block appended via agent for batchId={}, inspectionId={}",
                        inspection.getBatchId(), inspection.getId());
            } catch (Exception e) {
                log.error("[Blockchain] Failed to append block for batchId={}, inspectionId={} — scheduling retry",
                        inspection.getBatchId(), inspection.getId(), e);
                blockchainRetryService.scheduleRetry(
                        "BATCH", "INSPECTION", inspection.getId(), "CREATE",
                        snapshot, null, null, e.getMessage());
            }
        });

        try {
            var productionAgent = agentCoordinator.getProductionAgent();
            if (productionAgent.isAuthorized()) {
                if (event.isQualified()) {
                    productionAgent.updateCreditForQuality(90);
                    log.info("[Agent] ProductionAgent credit +10 for qualified inspection batchId={}", event.batchId());
                } else {
                    productionAgent.updateCreditForQuality(50);
                    log.warn("[Agent] ProductionAgent credit -20 for failed inspection batchId={}", event.batchId());
                }
            }
        } catch (Exception e) {
            log.error("[Agent] Failed to notify ProductionAgent", e);
        }
    }

    private void freezeSecurityCodes(Long batchId) {
        var codes = securityCodeRepo.findByBatch_Id(batchId);
        for (SecurityCode sc : codes) {
            sc.freeze();
        }
        securityCodeRepo.saveAll(codes);
        log.warn("[Event] 批次不合格，已冻结 {} 个防伪码 (batchId={})", codes.size(), batchId);
    }
}
