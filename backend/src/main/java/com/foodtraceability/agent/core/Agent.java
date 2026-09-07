package com.foodtraceability.agent.core;

import java.time.Instant;
import java.util.Map;

public interface Agent {
    String getAgentId();
    
    AgentType getAgentType();
    
    void initialize();
    
    void shutdown();
    
    boolean isAuthorized();
    
    Instant getRegisteredAt();
    
    long getCreditScore();

    void updateCreditScore(long delta);

    AgentState getState();

    Map<String, Object> getMetadata();
    
    enum AgentType {
        PRODUCTION("P-Agent"),
        CIRCULATION("C-Agent"),
        SALES("S-Agent"),
        CONSUMER("Consumer-Agent"),
        CERTIFICATE_AUTHORITY("CA-Agent"),
        ORDERER("O-Agent"),
        REGULATORY("Regulatory-Agent");
        
        private final String code;
        
        AgentType(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
    }
    
    enum AgentState {
        REGISTERED,
        CERTIFIED,
        ACTIVE,
        SUSPENDED,
        REVOKED
    }
}
