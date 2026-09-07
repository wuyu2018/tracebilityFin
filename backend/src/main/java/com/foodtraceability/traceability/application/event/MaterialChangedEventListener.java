package com.foodtraceability.traceability.application.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtraceability.agent.service.AgentBlockchainService;
import com.foodtraceability.repository.MaterialRepository;
import com.foodtraceability.service.BlockchainRetryService;
import com.foodtraceability.traceability.domain.event.MaterialChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MaterialChangedEventListener {

    private static final Logger log = LoggerFactory.getLogger(MaterialChangedEventListener.class);

    private final MaterialRepository repository;
    private final AgentBlockchainService agentBlockchainService;
    private final BlockchainRetryService blockchainRetryService;
    private final ObjectMapper objectMapper;

    public MaterialChangedEventListener(MaterialRepository repository,
                                         AgentBlockchainService agentBlockchainService,
                                         BlockchainRetryService blockchainRetryService) {
        this.repository = repository;
        this.agentBlockchainService = agentBlockchainService;
        this.blockchainRetryService = blockchainRetryService;
        this.objectMapper = new ObjectMapper();
    }
    // “原料变更”事件处理器：当原料信息发生变更（如新增、修改、停用）时，通过智能合约记录变更信息到区块链，同时如果是停用操作则通知相关代理进行后续处理。
    @TransactionalEventListener
    public void onMaterialChanged(MaterialChanged event) {
        log.info("[Event] MaterialChanged: id={}, action={}", event.materialId(), event.action());

        repository.findById(event.materialId()).ifPresent(material -> {
            String snapshotJson;
            try {
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("id", material.getId());
                snapshot.put("name", material.getName());
                snapshot.put("isActive", material.isActive());

                if ("DEACTIVATE".equals(event.action())) {
                    snapshot.put("is_deleted", true);
                }
                snapshotJson = objectMapper.writeValueAsString(snapshot);
            } catch (Exception e) {
                log.error("[Blockchain] Failed to build snapshot for Material id={}", material.getId(), e);
                return;
            }

            try {
                agentBlockchainService.appendBlockWithConsensus(
                        "MATERIAL", "MATERIAL", material.getId(), event.action(),
                        snapshotJson, null);
                log.info("[Blockchain] Material block appended via agent: id={}, action={}",
                        material.getId(), event.action());
            } catch (Exception e) {
                log.error("[Blockchain] Failed to append block for Material id={}, action={} — scheduling retry",
                        material.getId(), event.action(), e);
                blockchainRetryService.scheduleRetry(
                        "MATERIAL", "MATERIAL", material.getId(), event.action(),
                        snapshotJson, null, null, e.getMessage());
            }
        });
    }
}
