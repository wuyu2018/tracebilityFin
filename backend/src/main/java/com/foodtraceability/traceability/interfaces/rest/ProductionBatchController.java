package com.foodtraceability.traceability.interfaces.rest;

import com.foodtraceability.traceability.application.service.ProductionBatchApplicationService;
import com.foodtraceability.traceability.application.service.TraceabilityQueryApplicationService;
import com.foodtraceability.traceability.interfaces.dto.BatchResponse;
import com.foodtraceability.traceability.interfaces.dto.CreateBatchRequest;
import com.foodtraceability.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2")
@PreAuthorize("hasAnyRole('SUPER_ADMIN') or hasAuthority('AGENT_TYPE_PRODUCTION')")
public class ProductionBatchController {

    private static final Logger log = LoggerFactory.getLogger(ProductionBatchController.class);

    private final ProductionBatchApplicationService appService;
    private final TraceabilityQueryApplicationService queryService;
    private final SecurityUtils securityUtils;

    public ProductionBatchController(ProductionBatchApplicationService appService,
                                     TraceabilityQueryApplicationService queryService,
                                     SecurityUtils securityUtils) {
        this.appService = appService;
        this.queryService = queryService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/batches")
    public ResponseEntity<?> listBatches(@RequestParam(required = false) Long productId) {
        try {
            return ResponseEntity.ok(appService.listBatches(productId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/batches/{id}")
    public ResponseEntity<?> getBatch(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(appService.getBatch(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/batches/by-number/{batchNumber}")
    public ResponseEntity<?> getBatchByNumber(@PathVariable String batchNumber) {
        try {
            return ResponseEntity.ok(appService.getBatchByBatchNumber(batchNumber));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/batches/{id}")
    public ResponseEntity<?> deleteBatch(@PathVariable Long id) {
        try {
            if (!securityUtils.checkDataAccess("blockchain_log:" + id, "DELETE")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "无权限删除该批次数据"));
            }
            appService.deleteBatch(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
        } catch (Exception e) {
            log.error("[v2] 删除批次失败 - ID: {}, {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/batches/select-options")
    public ResponseEntity<?> getSelectOptions(@RequestParam(required = false) String keyword) {
        try {
            return ResponseEntity.ok(appService.getSelectOptions(keyword));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/batches")
    public ResponseEntity<?> createBatch(@RequestBody CreateBatchRequest req) {
        log.info("[v2] 创建批次 productId={}", req.getProductId());
        try {
            var result = appService.createBatch(req.toAppRequest());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new BatchResponse(result.id(), result.batchNumber(), result.productName()));
        } catch (Exception e) {
            log.error("[v2] 创建批次失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/batches/{id}/detail")
    public ResponseEntity<?> getBatchDetail(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(queryService.getBatchDetail(id));
        } catch (Exception e) {
            log.error("[v2] 获取批次详情失败 - ID: {}, {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
