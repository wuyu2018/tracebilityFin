package com.foodtraceability.traceability.application.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtraceability.agent.service.AgentBlockchainService;
import com.foodtraceability.repository.MaterialPurchaseRepository;
import com.foodtraceability.service.BlockchainRetryService;
import com.foodtraceability.traceability.domain.event.MaterialPurchaseChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MaterialPurchaseChangedEventListener {

    private static final Logger log = LoggerFactory.getLogger(MaterialPurchaseChangedEventListener.class);

    private final MaterialPurchaseRepository repository;
    private final AgentBlockchainService agentBlockchainService;
    private final BlockchainRetryService blockchainRetryService;
    private final ObjectMapper objectMapper;

    public MaterialPurchaseChangedEventListener(MaterialPurchaseRepository repository,
                                                  AgentBlockchainService agentBlockchainService,
                                                  BlockchainRetryService blockchainRetryService) {
        this.repository = repository;
        this.agentBlockchainService = agentBlockchainService;
        this.blockchainRetryService = blockchainRetryService;
        this.objectMapper = new ObjectMapper();
    }
    // “原料变更”事件处理器：当原料信息发生变更（如新增、修改、停用）时，通过智能合约记录变更信息到区块链，同时如果是停用操作则通知相关代理进行后续处理。
    @TransactionalEventListener
    public void onMaterialPurchaseChanged(MaterialPurchaseChanged event) {
        log.info("[Event] MaterialPurchaseChanged: id={}, action={}", event.purchaseId(), event.action());

        repository.findById(event.purchaseId()).ifPresent(purchase -> {
            String snapshotJson;
            try {
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("id", purchase.getId());
                snapshot.put("materialId", purchase.getMaterial().getId());
                snapshot.put("materialName", purchase.getMaterialName());
                snapshot.put("batchNumber", purchase.getBatchNumber());
                snapshot.put("supplierName", purchase.getSupplierName());
                snapshot.put("producerName", purchase.getProducerName());
                snapshot.put("producerAddress", purchase.getProducerAddress());
                snapshot.put("purchaseDate", purchase.getPurchaseDate() != null
                        ? purchase.getPurchaseDate().toString() : null);
                snapshot.put("quantity", purchase.getQuantity());
                snapshot.put("unit", purchase.getUnit());
                snapshot.put("is_deleted", purchase.isDeleted());
                snapshotJson = objectMapper.writeValueAsString(snapshot);
            } catch (Exception e) {
                log.error("[Blockchain] Failed to build snapshot for MaterialPurchase id={}",
                        purchase.getId(), e);
                return;
            }

            try {
                agentBlockchainService.appendBlockWithConsensus(
                        "MATERIAL", "MATERIAL_PURCHASE", purchase.getId(), event.action(),
                        snapshotJson, null);
                log.info("[Blockchain] MaterialPurchase block appended via agent: id={}, action={}",
                        purchase.getId(), event.action());
            } catch (Exception e) {
                log.error("[Blockchain] Failed to append block for MaterialPurchase id={}, action={} — scheduling retry",
                        purchase.getId(), event.action(), e);
                blockchainRetryService.scheduleRetry(
                        "MATERIAL", "MATERIAL_PURCHASE", purchase.getId(), event.action(),
                        snapshotJson, null, null, e.getMessage());
            }
        });
    }
}
