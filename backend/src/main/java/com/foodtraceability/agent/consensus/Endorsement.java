package com.foodtraceability.agent.consensus;

public record Endorsement(String agentId, boolean approved, String signature, String reason) {
}
