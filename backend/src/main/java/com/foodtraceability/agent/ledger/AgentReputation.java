package com.foodtraceability.agent.ledger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class AgentReputation {
    private final String agentId;
    private final Instant registeredAt;
    private final AtomicLong totalScore;
    private final List<ReputationHistory> history;
    private volatile long threshold = 50;
    
    public AgentReputation(String agentId) {
        this.agentId = agentId;
        this.registeredAt = Instant.now();
        this.totalScore = new AtomicLong(100);
        this.history = new ArrayList<>();
        this.history.add(new ReputationHistory(100, "Initial reputation"));
    }
    
    public void addScore(long delta) {
        long newScore = totalScore.addAndGet(delta);
        history.add(new ReputationHistory(delta, delta >= 0 ? "Positive behavior" : "Negative behavior"));
        
        if (newScore < 0) {
            totalScore.set(0);
        }
    }
    
    public long getTotalScore() {
        return totalScore.get();
    }
    
    public Instant getRegisteredAt() {
        return registeredAt;
    }
    
    public List<ReputationHistory> getHistory() {
        return new ArrayList<>(history);
    }
    
    public boolean isAboveThreshold() {
        return totalScore.get() >= threshold;
    }
    
    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public static class ReputationHistory {
        private final long scoreChange;
        private final String reason;
        private final Instant timestamp;
        
        public ReputationHistory(long scoreChange, String reason) {
            this.scoreChange = scoreChange;
            this.reason = reason;
            this.timestamp = Instant.now();
        }
        
        public long getScoreChange() {
            return scoreChange;
        }
        
        public String getReason() {
            return reason;
        }
        
        public Instant getTimestamp() {
            return timestamp;
        }
    }
}
