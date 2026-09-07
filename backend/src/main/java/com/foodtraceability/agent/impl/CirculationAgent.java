package com.foodtraceability.agent.impl;

import com.foodtraceability.agent.core.AbstractAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CirculationAgent extends AbstractAgent {
    
    private static final Logger log = LoggerFactory.getLogger(CirculationAgent.class);
    
    public CirculationAgent() {
        super("C-" + System.currentTimeMillis(), AgentType.CIRCULATION);
    }
    
    public CirculationAgent(String agentId) {
        super(agentId, AgentType.CIRCULATION);
    }
    
    @Override
    public void initialize() {
        super.initialize();
        setState(AgentState.CERTIFIED);
        addMetadata("capability", "food_circulation");
        addMetadata("service_type", "logistics");
        log.info("Circulation Agent initialized: {}", getAgentId());
    }
    
    public void recordTransport(String batchId, String origin, String destination) {
        if (!isAuthorized()) {
            throw new IllegalStateException("Agent not authorized");
        }
        
        log.info("Recording transport: batch={}, from={} to={}", batchId, origin, destination);
        addMetadata("last_batch_id", batchId);
        addMetadata("last_origin", origin);
        addMetadata("last_destination", destination);
    }
    
    public void recordStorage(String batchId, String warehouseId) {
        if (!isAuthorized()) {
            throw new IllegalStateException("Agent not authorized");
        }
        
        log.info("Recording storage: batch={}, warehouse={}", batchId, warehouseId);
        addMetadata("last_stored_batch", batchId);
        addMetadata("last_warehouse", warehouseId);
    }
    
    public void updateCreditForTimeliness(boolean onTime) {
        if (onTime) {
            updateCreditScore(5);
            log.info("Delivery on time - Credit increased");
        } else {
            updateCreditScore(-10);
            log.info("Delivery delayed - Credit decreased");
        }
    }
    
    @Override
    public void shutdown() {
        setState(AgentState.SUSPENDED);
        log.info("Circulation Agent shutdown: {}", getAgentId());
    }
}
