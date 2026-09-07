package com.foodtraceability.agent.core;

import com.foodtraceability.agent.consensus.PbftConsensus;
import com.foodtraceability.agent.credential.CertificateAuthority;
import com.foodtraceability.agent.impl.CertificateAuthorityAgent;
import com.foodtraceability.agent.impl.CirculationAgent;
import com.foodtraceability.agent.impl.ProductionAgent;
import com.foodtraceability.agent.impl.SalesAgent;
import com.foodtraceability.agent.ledger.ServiceLedger;
import com.foodtraceability.agent.ledger.TransactionLedger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MultiAgentCoordinator {
    
    private static final Logger log = LoggerFactory.getLogger(MultiAgentCoordinator.class);
    
    private final Map<String, Agent> agents;
    private final List<String> consensusReplicas;
    
    private final CertificateAuthority certificateAuthority;
    private final PbftConsensus pbftConsensus;
    private final ServiceLedger serviceLedger;
    private final TransactionLedger transactionLedger;
    
    private final ProductionAgent productionAgent;
    private final CirculationAgent circulationAgent;
    private final SalesAgent salesAgent;
    private final CertificateAuthorityAgent caAgent;
    
    public MultiAgentCoordinator(
            CertificateAuthority certificateAuthority,
            PbftConsensus pbftConsensus,
            ServiceLedger serviceLedger,
            TransactionLedger transactionLedger,
            ProductionAgent productionAgent,
            CirculationAgent circulationAgent,
            SalesAgent salesAgent,
            CertificateAuthorityAgent caAgent) {
        
        this.agents = new ConcurrentHashMap<>();
        this.consensusReplicas = new ArrayList<>();
        this.certificateAuthority = certificateAuthority;
        this.pbftConsensus = pbftConsensus;
        this.serviceLedger = serviceLedger;
        this.transactionLedger = transactionLedger;
        this.productionAgent = productionAgent;
        this.circulationAgent = circulationAgent;
        this.salesAgent = salesAgent;
        this.caAgent = caAgent;
    }
    
    public void initialize() {
        consensusReplicas.clear();
        agents.clear();
        log.info("Initializing Multi-Agent System...");
        
        caAgent.initialize();
        agents.put(caAgent.getAgentId(), caAgent);
        consensusReplicas.add(caAgent.getAgentId());
        caAgent.registerAgent(caAgent.getAgentId(), "CA-Agent", 365);

        productionAgent.initialize();
        agents.put(productionAgent.getAgentId(), productionAgent);
        consensusReplicas.add(productionAgent.getAgentId());
        caAgent.registerAgent(productionAgent.getAgentId(), "Production-Agent", 365);

        circulationAgent.initialize();
        agents.put(circulationAgent.getAgentId(), circulationAgent);
        consensusReplicas.add(circulationAgent.getAgentId());
        caAgent.registerAgent(circulationAgent.getAgentId(), "Circulation-Agent", 365);

        salesAgent.initialize();
        agents.put(salesAgent.getAgentId(), salesAgent);
        consensusReplicas.add(salesAgent.getAgentId());
        caAgent.registerAgent(salesAgent.getAgentId(), "Sales-Agent", 365);
        
        pbftConsensus.initialize(caAgent.getAgentId(), consensusReplicas);
        
        registerDefaultServices();
        
        log.info("Multi-Agent System initialized. Total agents: {}", agents.size());
    }
    
    private void registerDefaultServices() {
        serviceLedger.addService(new ServiceRecord(
            "SVC-PROD-001",
            productionAgent.getAgentId(),
            "production",
            "Food production and batch recording"
        ));
        
        serviceLedger.addService(new ServiceRecord(
            "SVC-CIRC-001",
            circulationAgent.getAgentId(),
            "logistics",
            "Food transportation and storage"
        ));
        
        serviceLedger.addService(new ServiceRecord(
            "SVC-SALES-001",
            salesAgent.getAgentId(),
            "retail",
            "Food sales and order management"
        ));
        
        log.info("Default services registered");
    }
    
    public Agent getAgent(String agentId) {
        return agents.get(agentId);
    }
    
    public Collection<Agent> getAllAgents() {
        return agents.values();
    }
    
    public PbftConsensus getPbftConsensus() {
        return pbftConsensus;
    }
    
    public ServiceLedger getServiceLedger() {
        return serviceLedger;
    }
    
    public TransactionLedger getTransactionLedger() {
        return transactionLedger;
    }
    
    public ProductionAgent getProductionAgent() {
        return productionAgent;
    }
    
    public CirculationAgent getCirculationAgent() {
        return circulationAgent;
    }
    
    public SalesAgent getSalesAgent() {
        return salesAgent;
    }
    
    public CertificateAuthorityAgent getCaAgent() {
        return caAgent;
    }
    
    public void shutdown() {
        log.info("Shutting down Multi-Agent System...");
        for (Agent agent : agents.values()) {
            agent.shutdown();
        }
        log.info("Multi-Agent System shutdown complete");
    }
}
