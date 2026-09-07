package com.foodtraceability.agent.contract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DataOnChainContract implements SmartContract {
    
    private static final Logger log = LoggerFactory.getLogger(DataOnChainContract.class);
    
    @Override
    public String getContractId() {
        return "CONTRACT-DATA-003";
    }
    
    @Override
    public ContractType getContractType() {
        return ContractType.DATA_ONCHAIN;
    }
    
    @Override
    public boolean execute(String context) {
        DataOnChainContext dataContext = parseContext(context);
        if (dataContext == null) {
            log.warn("Invalid data on-chain context");
            return false;
        }
        
        log.info("Data on-chain executed: type={}, id={}, action={}, hash={}", 
                dataContext.entityType, dataContext.entityId, dataContext.action, dataContext.dataHash);
        
        return true;
    }
    
    @Override
    public boolean validate(String context) {
        DataOnChainContext dataContext = parseContext(context);
        if (dataContext == null) {
            return false;
        }
        
        boolean valid = dataContext.entityType != null &&
                       dataContext.entityId != null &&
                       dataContext.action != null &&
                       dataContext.dataHash != null;
        
        if (!valid) {
            log.warn("Invalid data on-chain parameters");
        }
        
        return valid;
    }
    
    private DataOnChainContext parseContext(String context) {
        try {
            String[] parts = context.split("\\|");
            if (parts.length < 4) {
                return null;
            }
            return new DataOnChainContext(parts[0], Long.parseLong(parts[1]), parts[2], parts[3]);
        } catch (Exception e) {
            return null;
        }
    }
    
    private static class DataOnChainContext {
        String entityType;
        Long entityId;
        String action;
        String dataHash;
        
        DataOnChainContext(String entityType, Long entityId, String action, String dataHash) {
            this.entityType = entityType;
            this.entityId = entityId;
            this.action = action;
            this.dataHash = dataHash;
        }
    }
}
