package com.foodtraceability.agent.contract;

public interface SmartContract {
    String getContractId();
    
    ContractType getContractType();
    
    boolean execute(String context);
    
    boolean validate(String context);
    
    enum ContractType {
        PERMISSION_CONTROL,
        TRANSACTION_OPERATION,
        DATA_ONCHAIN,
        STATE_UPDATE
    }
}
