package com.foodtraceability.agent.contract;

import com.foodtraceability.agent.core.TransactionRecord;
import com.foodtraceability.agent.ledger.TransactionLedger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TransactionOperationContract implements SmartContract {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionOperationContract.class);
    
    private final TransactionLedger transactionLedger;
    
    public TransactionOperationContract(TransactionLedger transactionLedger) {
        this.transactionLedger = transactionLedger;
    }
    
    @Override
    public String getContractId() {
        return "CONTRACT-TRANSACTION-002";
    }
    
    @Override
    public ContractType getContractType() {
        return ContractType.TRANSACTION_OPERATION;
    }
    
    @Override
    public boolean execute(String context) {
        TransactionContext txContext = parseContext(context);
        if (txContext == null) {
            log.warn("Invalid transaction context");
            return false;
        }
        
        String transactionId = UUID.randomUUID().toString();
        TransactionRecord record = new TransactionRecord(
            transactionId,
            txContext.requesterAgentId,
            txContext.providerAgentId,
            txContext.serviceType
        );
        
        transactionLedger.addTransaction(record);
        
        log.info("Transaction executed: id={}, requester={}, provider={}", 
                transactionId, txContext.requesterAgentId, txContext.providerAgentId);
        
        return true;
    }
    
    @Override
    public boolean validate(String context) {
        TransactionContext txContext = parseContext(context);
        if (txContext == null) {
            return false;
        }
        
        boolean valid = txContext.requesterAgentId != null &&
                       txContext.providerAgentId != null &&
                       txContext.serviceType != null;
        
        if (!valid) {
            log.warn("Invalid transaction parameters");
        }
        
        return valid;
    }
    
    private TransactionContext parseContext(String context) {
        try {
            String[] parts = context.split("\\|");
            if (parts.length < 3) {
                return null;
            }
            return new TransactionContext(parts[0], parts[1], parts[2]);
        } catch (Exception e) {
            return null;
        }
    }
    
    private static class TransactionContext {
        String requesterAgentId;
        String providerAgentId;
        String serviceType;
        
        TransactionContext(String requesterAgentId, String providerAgentId, String serviceType) {
            this.requesterAgentId = requesterAgentId;
            this.providerAgentId = providerAgentId;
            this.serviceType = serviceType;
        }
    }
}
