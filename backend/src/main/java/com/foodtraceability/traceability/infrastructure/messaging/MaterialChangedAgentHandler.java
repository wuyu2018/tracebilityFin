package com.foodtraceability.traceability.infrastructure.messaging;

import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.foodtraceability.traceability.domain.event.MaterialChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MaterialChangedAgentHandler {

    private static final Logger log = LoggerFactory.getLogger(MaterialChangedAgentHandler.class);

    private final MultiAgentCoordinator agentCoordinator;

    @Autowired
    public MaterialChangedAgentHandler(MultiAgentCoordinator agentCoordinator) {
        this.agentCoordinator = agentCoordinator;
    }
    // “原料变更”事件处理器：当原料信息发生变更（如新增、修改、停用）时，通过智能合约记录变更信息到区块链，同时如果是停用操作则通知相关代理进行后续处理。
    @TransactionalEventListener
    public void handleMaterialChanged(MaterialChanged event) {
        log.info("Handling MaterialChanged event: materialId={}, action={}", 
                event.materialId(), event.action());
        
        try {
            switch (event.action()) {
                case "CREATE":
                    handleMaterialCreation(event);
                    break;
                case "UPDATE":
                case "ACTIVATE":
                    handleMaterialUpdate(event);
                    break;
                case "DEACTIVATE":
                    handleMaterialDeletion(event);
                    break;
                default:
                    log.warn("Unknown material action: {}", event.action());
            }
        } catch (Exception e) {
            log.error("Failed to handle MaterialChanged event", e);
            throw e;
        }
    }
    
    private void handleMaterialCreation(MaterialChanged event) {
        var productionAgent = agentCoordinator.getProductionAgent();
        
        if (!productionAgent.isAuthorized()) {
            log.error("Production agent not authorized");
            throw new IllegalStateException("Production agent not authorized");
        }
        
        String traceabilityCode = productionAgent.generateTraceabilityCode(event.materialId());
        productionAgent.addMetadata("material_" + event.materialId(), traceabilityCode);
        
        log.info("Material created with traceability code: {}", traceabilityCode);
    }
    
    private void handleMaterialUpdate(MaterialChanged event) {
        var productionAgent = agentCoordinator.getProductionAgent();
        productionAgent.updateCreditScore(2);
        
        log.info("Material updated, credit score adjusted");
    }
    
    private void handleMaterialDeletion(MaterialChanged event) {
        var productionAgent = agentCoordinator.getProductionAgent();
        productionAgent.updateCreditScore(-5);
        
        log.info("Material deleted, credit score adjusted");
    }
}
