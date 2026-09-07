package com.foodtraceability.controller;

import com.foodtraceability.service.BlockchainMonitorService;
import com.foodtraceability.service.BlockchainRetryService;
import com.foodtraceability.service.BlockchainService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/blockchain")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class BlockchainController {

    private final BlockchainService blockchainService;
    private final BlockchainMonitorService blockchainMonitorService;
    private final BlockchainRetryService blockchainRetryService;

    public BlockchainController(BlockchainService blockchainService,
                                BlockchainMonitorService blockchainMonitorService,
                                BlockchainRetryService blockchainRetryService) {
        this.blockchainService = blockchainService;
        this.blockchainMonitorService = blockchainMonitorService;
        this.blockchainRetryService = blockchainRetryService;
    }

    @GetMapping("/public-key")
    public ResponseEntity<?> getPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", blockchainService.getPublicKeyBase64()));
    }

    @GetMapping("/monitor/summary")
    public ResponseEntity<Map<String, Object>> getMonitorSummary() {
        return ResponseEntity.ok(blockchainMonitorService.getSummary());
    }

    @GetMapping("/retry-tasks")
    public ResponseEntity<?> getRetryTasks(@RequestParam(defaultValue = "pending") String status) {
        Map<String, Object> result = new LinkedHashMap<>();
        if ("failed".equalsIgnoreCase(status)) {
            result.put("tasks", blockchainRetryService.getFailedTasks());
            result.put("count", blockchainRetryService.getFailedCount());
        } else {
            result.put("tasks", blockchainRetryService.getPendingTasks());
            result.put("count", blockchainRetryService.getPendingCount());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/retry-tasks/summary")
    public ResponseEntity<?> getRetryTaskSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("pendingCount", blockchainRetryService.getPendingCount());
        summary.put("failedCount", blockchainRetryService.getFailedCount());
        return ResponseEntity.ok(summary);
    }

}
