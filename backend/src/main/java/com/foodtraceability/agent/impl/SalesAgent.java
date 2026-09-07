package com.foodtraceability.agent.impl;

import com.foodtraceability.agent.core.AbstractAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SalesAgent extends AbstractAgent {
    
    private static final Logger log = LoggerFactory.getLogger(SalesAgent.class);
    
    public SalesAgent() {
        super("S-" + System.currentTimeMillis(), AgentType.SALES);
    }
    
    public SalesAgent(String agentId) {
        super(agentId, AgentType.SALES);
    }
    
    @Override
    public void initialize() {
        super.initialize();
        setState(AgentState.CERTIFIED);
        addMetadata("capability", "food_sales");
        addMetadata("service_type", "retail");
        log.info("Sales Agent initialized: {}", getAgentId());
    }
    
    public void recordSale(String batchId, String traceabilityCode, Long quantity) {
        if (!isAuthorized()) {
            throw new IllegalStateException("Agent not authorized");
        }
        
        log.info("Recording sale: batch={}, traceCode={}, quantity={}", batchId, traceabilityCode, quantity);
        addMetadata("last_sold_batch", batchId);
        addMetadata("last_trace_code", traceabilityCode);
    }
    
    public void placeOrder(String productId, Long quantity, String requiredDeliveryDate) {
        if (!isAuthorized()) {
            throw new IllegalStateException("Agent not authorized");
        }
        
        log.info("Placing order: product={}, quantity={}, delivery={}", productId, quantity, requiredDeliveryDate);
        addMetadata("pending_order_product", productId);
        addMetadata("pending_order_quantity", quantity.toString());
    }
    
    public void updateCreditForService(long customerSatisfaction) {
        if (customerSatisfaction >= 4) {
            updateCreditScore(5);
            log.info("Customer satisfaction {} - Credit increased", customerSatisfaction);
        } else if (customerSatisfaction <= 2) {
            updateCreditScore(-15);
            log.info("Customer satisfaction {} - Credit decreased", customerSatisfaction);
        }
    }
    
    @Override
    public void shutdown() {
        setState(AgentState.SUSPENDED);
        log.info("Sales Agent shutdown: {}", getAgentId());
    }
}
