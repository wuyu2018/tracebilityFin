package com.foodtraceability.config;

import com.foodtraceability.agent.core.MultiAgentCoordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Configuration
@EnableAsync
public class AgentSystemConfig {
    
    private static final Logger log = LoggerFactory.getLogger(AgentSystemConfig.class);
    
    private final MultiAgentCoordinator agentCoordinator;
    
    public AgentSystemConfig(MultiAgentCoordinator agentCoordinator) {
        this.agentCoordinator = agentCoordinator;
    }
    
    @PostConstruct
    public void startAgentSystem() {
        log.info("Starting Agent System...");
        agentCoordinator.initialize();
        log.info("Agent System started successfully");
    }
    
    @PreDestroy
    public void stopAgentSystem() {
        log.info("Stopping Agent System...");
        agentCoordinator.shutdown();
        log.info("Agent System stopped");
    }
}
