package com.foodtraceability.traceability.application.event;

import com.foodtraceability.agent.service.AgentBlockchainService;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.service.BlockchainRetryService;
import com.foodtraceability.traceability.domain.event.ProductChanged;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ProductChangedEventListener {

    private static final Logger log = LoggerFactory.getLogger(ProductChangedEventListener.class);

    private final ProductRepository productRepo;
    private final AgentBlockchainService agentBlockchainService;
    private final BlockchainRetryService blockchainRetryService;

    public ProductChangedEventListener(ProductRepository productRepo,
                                        AgentBlockchainService agentBlockchainService,
                                        BlockchainRetryService blockchainRetryService) {
        this.productRepo = productRepo;
        this.agentBlockchainService = agentBlockchainService;
        this.blockchainRetryService = blockchainRetryService;
    }

    @TransactionalEventListener
    public void onProductChanged(ProductChanged event) {
        log.info("[Event] ProductChanged: productId={}, action={}", event.productId(), event.action());
        productRepo.findById(event.productId()).ifPresent(product -> {
            String snapshot;
            try {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("id", product.getId());
                data.put("name", product.getName());
                data.put("specification", product.getSpecification());
                data.put("shelfLife", product.getShelfLife());
                data.put("imageUrl", product.getImageUrl());
                data.put("contactPhone", product.getContactPhone());
                data.put("contactEmail", product.getContactEmail());
                data.put("isDeleted", product.getIsDeleted());
                snapshot = new ObjectMapper().writeValueAsString(data);
            } catch (Exception e) {
                log.error("[Blockchain] Failed to build snapshot for Product id={}", product.getId(), e);
                return;
            }
            try {
                agentBlockchainService.appendBlockWithConsensus(
                        "BATCH", "PRODUCT", product.getId(), event.action(),
                        snapshot, null);
                log.info("[Blockchain] Product block appended via agent for productId={}, action={}",
                        product.getId(), event.action());
            } catch (Exception e) {
                log.error("[Blockchain] Failed to append block for productId={} — scheduling retry",
                        product.getId(), e);
                blockchainRetryService.scheduleRetry(
                        "BATCH", "PRODUCT", product.getId(), event.action(),
                        snapshot, null, null, e.getMessage());
            }
        });
    }
}
