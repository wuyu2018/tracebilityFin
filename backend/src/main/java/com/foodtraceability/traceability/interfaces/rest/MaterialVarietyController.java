package com.foodtraceability.traceability.interfaces.rest;

import com.foodtraceability.aop.OperationLog;
import com.foodtraceability.traceability.application.service.MaterialApplicationService;
import com.foodtraceability.traceability.interfaces.dto.CreateMaterialVarietyRequest;
import com.foodtraceability.traceability.interfaces.dto.MaterialVarietyResponse;
import com.foodtraceability.traceability.interfaces.dto.UpdateMaterialVarietyRequest;
import com.foodtraceability.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/material-varieties")
@PreAuthorize("hasAnyRole('SUPER_ADMIN') or hasAuthority('AGENT_TYPE_PRODUCTION')")
public class MaterialVarietyController {

    private static final Logger log = LoggerFactory.getLogger(MaterialVarietyController.class);

    private final MaterialApplicationService appService;
    private final SecurityUtils securityUtils;

    public MaterialVarietyController(MaterialApplicationService appService, SecurityUtils securityUtils) {
        this.appService = appService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    @OperationLog(entityType = "MATERIAL", action = "CREATE")
    public ResponseEntity<?> createMaterialVariety(@RequestBody CreateMaterialVarietyRequest req) {
        log.info("[V2] 创建物料品种: {}", req.getName());
        try {
            var result = appService.createMaterial(req.toAppRequest());
            return ResponseEntity.status(HttpStatus.CREATED).body(MaterialVarietyResponse.from(result));
        } catch (Exception e) {
            log.error("[V2] 创建物料品种失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listMaterialVarieties(
            @RequestParam(required = false) Boolean activeOnly) {
        try {
            return ResponseEntity.ok(
                    appService.listMaterials(activeOnly).stream()
                            .map(MaterialVarietyResponse::from).toList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMaterialVariety(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(MaterialVarietyResponse.from(appService.getMaterial(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @OperationLog(entityType = "MATERIAL", action = "UPDATE")
    public ResponseEntity<?> updateMaterialVariety(@PathVariable Long id,
                                                    @RequestBody UpdateMaterialVarietyRequest req) {
        log.info("[V2] 更新物料品种: id={}", id);
        try {
            var result = appService.updateMaterial(id, req.toAppRequest());
            return ResponseEntity.ok(MaterialVarietyResponse.from(result));
        } catch (Exception e) {
            log.error("[V2] 更新物料品种失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @OperationLog(entityType = "MATERIAL", action = "DELETE")
    public ResponseEntity<?> deleteMaterialVariety(@PathVariable Long id) {
        log.info("[V2] 删除物料品种: id={}", id);
        try {
            if (!securityUtils.checkDataAccess("blockchain_log:" + id, "DELETE")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "无权限删除该物料数据"));
            }
            appService.deleteMaterial(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("[V2] 删除物料品种失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/activate")
    @OperationLog(entityType = "MATERIAL", action = "ACTIVATE")
    public ResponseEntity<?> activateMaterialVariety(@PathVariable Long id) {
        log.info("[V2] 启用物料品种: id={}", id);
        try {
            return ResponseEntity.ok(
                    MaterialVarietyResponse.from(appService.activateMaterial(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/deactivate")
    @OperationLog(entityType = "MATERIAL", action = "DEACTIVATE")
    public ResponseEntity<?> deactivateMaterialVariety(@PathVariable Long id) {
        log.info("[V2] 停用物料品种: id={}", id);
        try {
            return ResponseEntity.ok(
                    MaterialVarietyResponse.from(appService.deactivateMaterial(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
