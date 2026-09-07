package com.foodtraceability.agent.contract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StateUpdateContract implements SmartContract {
    
    private static final Logger log = LoggerFactory.getLogger(StateUpdateContract.class);
    
    @Override
    public String getContractId() {
        return "CONTRACT-STATE-004";
    }
    
    @Override
    public ContractType getContractType() {
        return ContractType.STATE_UPDATE;
    }
    
    @Override
    public boolean execute(String context) {
        StateUpdateContext stateContext = parseContext(context);
        if (stateContext == null) {
            log.warn("Invalid state update context");
            return false;
        }
        
        log.info("State update executed: key={}, oldValue={}, newValue={}", 
                stateContext.stateKey, stateContext.oldValue, stateContext.newValue);
        
        return true;
    }
    
    @Override
    public boolean validate(String context) {
        StateUpdateContext stateContext = parseContext(context);
        if (stateContext == null) {
            return false;
        }
        
        boolean valid = stateContext.stateKey != null &&
                       stateContext.newValue != null;
        
        if (!valid) {
            log.warn("Invalid state update parameters");
        }
        
        return valid;
    }
    
    private StateUpdateContext parseContext(String context) {
        try {
            String[] parts = context.split("\\|");
            if (parts.length < 2) {
                return null;
            }
            String oldValue = parts.length > 2 ? parts[2] : null;
            return new StateUpdateContext(parts[0], parts[1], oldValue);
        } catch (Exception e) {
            return null;
        }
    }
    
    private static class StateUpdateContext {
        String stateKey;
        String newValue;
        String oldValue;
        
        StateUpdateContext(String stateKey, String newValue, String oldValue) {
            this.stateKey = stateKey;
            this.newValue = newValue;
            this.oldValue = oldValue;
        }
    }
}
