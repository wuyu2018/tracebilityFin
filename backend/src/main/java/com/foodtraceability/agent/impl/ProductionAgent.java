package com.foodtraceability.agent.impl;

import com.foodtraceability.agent.core.AbstractAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductionAgent extends AbstractAgent {
    
    private static final Logger log = LoggerFactory.getLogger(ProductionAgent.class);
    
    public ProductionAgent() {
        super("P-" + System.currentTimeMillis(), AgentType.PRODUCTION);
    }
    
    public ProductionAgent(String agentId) {
        super(agentId, AgentType.PRODUCTION);
    }
    
    @Override
    public void initialize() {
        super.initialize();
        setState(AgentState.CERTIFIED);
        addMetadata("capability", "food_production");
        addMetadata("service_type", "production");
        log.info("Production Agent initialized: {}", getAgentId());
    }
    
    public void recordProductionBatch(Long batchId, String productInfo) {
        if (!isAuthorized()) {
            throw new IllegalStateException("Agent not authorized");
        }
        
        log.info("Recording production batch: {}, product: {}", batchId, productInfo);
        addMetadata("last_batch_id", batchId.toString());
        addMetadata("last_product_info", productInfo);
    }
    
    public String generateTraceabilityCode(Long batchId) {
        return "TRC-P-" + batchId + "-" + System.currentTimeMillis();
    }
    
    public void updateCreditForQuality(long qualityScore) {
        if (qualityScore >= 90) {
            updateCreditScore(10);
            log.info("Quality score {} - Credit increased", qualityScore);
        } else if (qualityScore < 60) {
            updateCreditScore(-20);
            log.info("Quality score {} - Credit decreased", qualityScore);
        }
    }
    
    @Override
    public void shutdown() {
        setState(AgentState.SUSPENDED);
        log.info("Production Agent shutdown: {}", getAgentId());
    }
}
