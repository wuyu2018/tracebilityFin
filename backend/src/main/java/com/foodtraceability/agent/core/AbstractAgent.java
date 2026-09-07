package com.foodtraceability.agent.core;

import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractAgent implements Agent {
    
    protected final String agentId;
    protected final AgentType agentType;
    protected final Instant registeredAt;
    protected final AtomicLong creditScore;
    protected volatile AgentState state;
    protected X509Certificate certificate;
    protected final Map<String, Object> metadata;
    
    protected AbstractAgent(String agentId, AgentType agentType) {
        this.agentId = agentId;
        this.agentType = agentType;
        this.registeredAt = Instant.now();
        this.creditScore = new AtomicLong(100);
        this.state = AgentState.REGISTERED;
        this.metadata = new ConcurrentHashMap<>();
    }
    
    @Override
    public String getAgentId() {
        return agentId;
    }
    
    @Override
    public AgentType getAgentType() {
        return agentType;
    }
    
    @Override
    public Instant getRegisteredAt() {
        return registeredAt;
    }
    
    @Override
    public long getCreditScore() {
        return creditScore.get();
    }
    
    @Override
    public void updateCreditScore(long delta) {
        long newScore = creditScore.addAndGet(delta);
        if (newScore < 0) {
            creditScore.set(0);
        }
    }
    
    @Override
    public boolean isAuthorized() {
        return state == AgentState.CERTIFIED || state == AgentState.ACTIVE;
    }
    
    public AgentState getState() {
        return state;
    }
    
    public void setState(AgentState state) {
        this.state = state;
    }
    
    public X509Certificate getCertificate() {
        return certificate;
    }
    
    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    protected Object getMetadataValue(String key) {
        return this.metadata.get(key);
    }
    
    @Override
    public void initialize() {
    }
    
    @Override
    public void shutdown() {
    }
}
