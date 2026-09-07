package com.foodtraceability.controller.builder;

import com.foodtraceability.dto.TraceInfoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Centralizes response payload assembly for traceability verification endpoints.
 * Keeping the controller focused on orchestration/flow while response format remains consistent.
 */
@Component
public class TraceabilityResponseBuilder {

    private static final String MSG_SYSTEM_ERROR = "系统错误，请稍后重试";

    public ResponseEntity<Map<String, Object>> invalid(String message) {
        return ResponseEntity.ok(Map.of(
                "valid", false,
                "message", message
        ));
    }

    public ResponseEntity<Map<String, Object>> systemError() {
        return invalid(MSG_SYSTEM_ERROR);
    }

    public ResponseEntity<Map<String, Object>> validTrace(TraceInfoDTO traceInfo) {
        return ResponseEntity.ok(Map.of("valid", true, "data", traceInfo));
    }
}

