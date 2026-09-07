package com.foodtraceability.traceability.application.event;

import com.foodtraceability.agent.service.AgentBlockchainService;
import com.foodtraceability.repository.SecurityCodeRepository;
import com.foodtraceability.service.BlockchainRetryService;
import com.foodtraceability.traceability.domain.event.SecurityCodesGenerated;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SecurityCodesGeneratedEventListener {

    private static final Logger log = LoggerFactory.getLogger(SecurityCodesGeneratedEventListener.class);

    private final SecurityCodeRepository securityCodeRepo;
    private final AgentBlockchainService agentBlockchainService;
    private final BlockchainRetryService blockchainRetryService;

    public SecurityCodesGeneratedEventListener(SecurityCodeRepository securityCodeRepo,
                                                AgentBlockchainService agentBlockchainService,
                                                BlockchainRetryService blockchainRetryService) {
        this.securityCodeRepo = securityCodeRepo;
        this.agentBlockchainService = agentBlockchainService;
        this.blockchainRetryService = blockchainRetryService;
    }

    @TransactionalEventListener
    public void onSecurityCodesGenerated(SecurityCodesGenerated event) {
        log.info("[Event] SecurityCodesGenerated: batchId={}, quantity={}", event.batchId(), event.quantity());
        var codes = securityCodeRepo.findByBatch_Id(event.batchId());
        String snapshot;
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("batchId", event.batchId());
            data.put("quantity", event.quantity());
            data.put("codeCount", codes.size());
            if (!codes.isEmpty()) {
                data.put("batchNumber", codes.get(0).getBatch().getBatchNumber());
            }
            snapshot = new ObjectMapper().writeValueAsString(data);
        } catch (Exception e) {
            log.error("[Blockchain] Failed to build snapshot for SecurityCode batchId={}", event.batchId(), e);
            return;
        }
        try {
            agentBlockchainService.appendBlockWithConsensus(
                    "BATCH", "SECURITY_CODE", event.batchId(), "CREATE",
                    snapshot, null);
            log.info("[Blockchain] SecurityCode block appended via agent for batchId={}", event.batchId());
        } catch (Exception e) {
            log.error("[Blockchain] Failed to append block for batchId={} — scheduling retry",
                    event.batchId(), e);
            blockchainRetryService.scheduleRetry(
                    "BATCH", "SECURITY_CODE", event.batchId(), "CREATE",
                    snapshot, null, null, e.getMessage());
        }
    }
}
