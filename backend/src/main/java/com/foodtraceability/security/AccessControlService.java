package com.foodtraceability.security;

import com.foodtraceability.agent.core.Agent;
import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.foodtraceability.entity.OffchainStorage;
import com.foodtraceability.repository.OffchainStorageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {

    private static final Logger log = LoggerFactory.getLogger(AccessControlService.class);
    private static final long MIN_CREDIT_SCORE = 50;

    private final MultiAgentCoordinator agentCoordinator;
    private final OffchainStorageRepository offchainStorageRepo;
    private final ObjectMapper objectMapper;

    public AccessControlService(MultiAgentCoordinator agentCoordinator,
                                OffchainStorageRepository offchainStorageRepo) {
        this.agentCoordinator = agentCoordinator;
        this.offchainStorageRepo = offchainStorageRepo;
        this.objectMapper = new ObjectMapper();
    }

    public boolean hasPermission(String foodId, String action, String agentId) {
        try {
            Agent agent = agentCoordinator.getAgent(agentId);
            if (agent == null) {
                log.warn("Agent not found: {}", agentId);
                return false;
            }

            if (!agent.isAuthorized()) {
                log.warn("Agent not authorized: {}", agentId);
                return false;
            }

            if (agent.getCreditScore() < MIN_CREDIT_SCORE) {
                log.warn("Agent credit score too low: {} (score={})", agentId, agent.getCreditScore());
                return false;
            }

            OffchainStorage storage = offchainStorageRepo.findActiveByFoodId(foodId).orElse(null);
            if (storage == null) {
                log.warn("Food data not found: {}", foodId);
                return false;
            }

            return checkPolicy(storage.getAccessPolicy(), agent.getAgentType().getCode(), action);

        } catch (Exception e) {
            log.error("Access control check failed", e);
            return false;
        }
    }

    private boolean checkPolicy(String accessPolicyJson, String agentTypeCode, String action) {
        if (accessPolicyJson == null || accessPolicyJson.isBlank()) {
            log.warn("No access policy defined");
            return false;
        }

        try {
            JsonNode root = objectMapper.readTree(accessPolicyJson);
            JsonNode rules = root.path("accessPolicy").path("rules");

            if (rules.isArray()) {
                for (JsonNode rule : rules) {
                    JsonNode agentTypes = rule.path("agentTypes");
                    if (!agentTypes.isArray()) continue;

                    boolean typeMatched = false;
                    for (JsonNode type : agentTypes) {
                        if (type.asText().equals(agentTypeCode)) {
                            typeMatched = true;
                            break;
                        }
                    }
                    if (!typeMatched) continue;

                    JsonNode permissions = rule.path("permissions");
                    if (permissions.isArray()) {
                        for (JsonNode perm : permissions) {
                            if (perm.asText().equals(action)) {
                                return true;
                            }
                        }
                    }
                }
            }

            log.debug("Access denied: agentType={}, action={}", agentTypeCode, action);
            return false;

        } catch (JsonProcessingException e) {
            log.error("Failed to parse access policy JSON", e);
            return false;
        }
    }

    public boolean isOwner(String foodId, String agentId) {
        try {
            OffchainStorage storage = offchainStorageRepo.findActiveByFoodId(foodId).orElse(null);
            if (storage == null) return false;

            Agent agent = agentCoordinator.getAgent(agentId);
            if (agent == null) return false;

            String ownerId = "P-" + storage.getOwnerAgentId();
            return agentId.equals(ownerId);

        } catch (Exception e) {
            log.error("Owner check failed", e);
            return false;
        }
    }
}
