package com.foodtraceability.controller;

import com.foodtraceability.agent.core.Agent;
import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.foodtraceability.agent.ledger.AgentReputation;
import com.foodtraceability.agent.consensus.PbftConsensus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/agent")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AgentController {
    
    private final MultiAgentCoordinator agentCoordinator;
    
    public AgentController(MultiAgentCoordinator agentCoordinator) {
        this.agentCoordinator = agentCoordinator;
    }
    
    @GetMapping("/list")
    public ResponseEntity<?> listAgents() {
        List<Map<String, Object>> agentList = agentCoordinator.getAllAgents().stream()
            .map(agent -> {
                Map<String, Object> agentInfo = new HashMap<>();
                agentInfo.put("agentId", agent.getAgentId());
                agentInfo.put("agentType", agent.getAgentType().getCode());
                agentInfo.put("state", agent.getState().name());
                agentInfo.put("creditScore", agent.getCreditScore());
                agentInfo.put("registeredAt", agent.getRegisteredAt().toString());
                agentInfo.put("authorized", agent.isAuthorized());
                agentInfo.put("metadata", agent.getMetadata());
                return agentInfo;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
            "totalAgents", agentList.size(),
            "agents", agentList
        ));
    }
    
    @GetMapping("/{agentId}")
    public ResponseEntity<?> getAgent(@PathVariable String agentId) {
        Agent agent = agentCoordinator.getAgent(agentId);
        if (agent == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(Map.of(
            "agentId", agent.getAgentId(),
            "agentType", agent.getAgentType().getCode(),
            "state", agent.getState().name(),
            "creditScore", agent.getCreditScore(),
            "registeredAt", agent.getRegisteredAt().toString(),
            "authorized", agent.isAuthorized(),
            "metadata", agent.getMetadata()
        ));
    }
    
    @GetMapping("/consensus/status")
    public ResponseEntity<?> getConsensusStatus() {
        PbftConsensus pbft = agentCoordinator.getPbftConsensus();
        
        return ResponseEntity.ok(Map.of(
            "nodeId", "node-" + agentCoordinator.getProductionAgent().getAgentId(),
            "isPrimary", pbft != null && pbft.toString().contains("Primary"),
            "view", "0",
            "consensusActive", true
        ));
    }
    
    @GetMapping("/reputation/list")
    public ResponseEntity<?> listReputations() {
        Iterable<AgentReputation> reputations = agentCoordinator.getTransactionLedger().getAllReputations();
        
        List<Map<String, Object>> reputationList = new ArrayList<>();
        for (AgentReputation reputation : reputations) {
            Map<String, Object> repInfo = new HashMap<>();
            repInfo.put("agentId", reputation.getAgentId());
            repInfo.put("totalScore", reputation.getTotalScore());
            repInfo.put("aboveThreshold", reputation.isAboveThreshold());
            repInfo.put("registeredAt", reputation.getRegisteredAt().toString());
            reputationList.add(repInfo);
        }
        
        return ResponseEntity.ok(Map.of(
            "totalReputations", reputationList.size(),
            "reputations", reputationList
        ));
    }
    
    @GetMapping("/{agentId}/reputation")
    public ResponseEntity<?> getAgentReputation(@PathVariable String agentId) {
        AgentReputation reputation = agentCoordinator.getTransactionLedger().getReputationRecord(agentId);
        if (reputation == null) {
            return ResponseEntity.ok(Map.of(
                "agentId", agentId,
                "totalScore", 0L,
                "aboveThreshold", false,
                "history", Collections.emptyList()
            ));
        }
        
        List<Map<String, Object>> history = reputation.getHistory().stream()
            .map(h -> {
                Map<String, Object> m = new HashMap<>();
                m.put("scoreChange", h.getScoreChange());
                m.put("reason", h.getReason());
                m.put("timestamp", h.getTimestamp().toString());
                return m;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
            "agentId", agentId,
            "totalScore", reputation.getTotalScore(),
            "aboveThreshold", reputation.isAboveThreshold(),
            "history", history
        ));
    }
}
