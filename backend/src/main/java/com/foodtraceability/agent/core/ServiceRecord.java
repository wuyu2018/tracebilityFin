package com.foodtraceability.agent.core;

import java.time.Instant;

public class ServiceRecord {
    private final String serviceId;
    private final String agentId;
    private final String serviceType;
    private final String description;
    private final Instant createdAt;
    private volatile boolean active;
    
    public ServiceRecord(String serviceId, String agentId, String serviceType, String description) {
        this.serviceId = serviceId;
        this.agentId = agentId;
        this.serviceType = serviceType;
        this.description = description;
        this.createdAt = Instant.now();
        this.active = true;
    }
    
    public String getServiceId() {
        return serviceId;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public String getServiceType() {
        return serviceType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}
