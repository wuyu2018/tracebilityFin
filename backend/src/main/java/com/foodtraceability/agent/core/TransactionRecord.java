package com.foodtraceability.agent.core;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

public class TransactionRecord {
    private final String transactionId;
    private final String requesterAgentId;
    private final String providerAgentId;
    private final String serviceType;
    private final Instant timestamp;
    private final TransactionStatus status;
    private final List<String> endorsementSignatures;
    private String responseHash;
    private long creditChange;
    
    public TransactionRecord(String transactionId, String requesterAgentId, 
                            String providerAgentId, String serviceType) {
        this.transactionId = transactionId;
        this.requesterAgentId = requesterAgentId;
        this.providerAgentId = providerAgentId;
        this.serviceType = serviceType;
        this.timestamp = Instant.now();
        this.status = TransactionStatus.PENDING;
        this.endorsementSignatures = new ArrayList<>();
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public String getRequesterAgentId() {
        return requesterAgentId;
    }
    
    public String getProviderAgentId() {
        return providerAgentId;
    }
    
    public String getServiceType() {
        return serviceType;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransactionStatus status) {
    }
    
    public List<String> getEndorsementSignatures() {
        return endorsementSignatures;
    }
    
    public void addEndorsementSignature(String signature) {
        this.endorsementSignatures.add(signature);
    }
    
    public String getResponseHash() {
        return responseHash;
    }
    
    public void setResponseHash(String responseHash) {
        this.responseHash = responseHash;
    }
    
    public long getCreditChange() {
        return creditChange;
    }
    
    public void setCreditChange(long creditChange) {
        this.creditChange = creditChange;
    }
    
    public enum TransactionStatus {
        PENDING,
        ENDORSED,
        COMMITTED,
        REJECTED,
        FAILED
    }
}
