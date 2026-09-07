package com.foodtraceability.controller;

import com.foodtraceability.aop.OperationLog;
import com.foodtraceability.dto.*;
import com.foodtraceability.entity.Product;
import com.foodtraceability.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataManagementController {

    private static final Logger log = LoggerFactory.getLogger(DataManagementController.class);

    private final ProductService productService;
    private final SecurityCodeService securityCodeService;
    private final com.foodtraceability.util.SecurityUtils securityUtils;

    public DataManagementController(ProductService productService,
                                   SecurityCodeService securityCodeService,
                                   com.foodtraceability.util.SecurityUtils securityUtils) {
        this.productService = productService;
        this.securityCodeService = securityCodeService;
        this.securityUtils = securityUtils;
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN') or hasAuthority('AGENT_TYPE_PRODUCTION')")
    @PostMapping("/products")
    @OperationLog(entityType = "PRODUCT", action = "CREATE")
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO dto) {
        log.info("[产品管理] 创建产品 - 名称: {}", dto.getName());
        try {
            // companyId removed
            Product created = productService.createProduct(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("[产品管理] 创建产品失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN') or hasAuthority('AGENT_TYPE_PRODUCTION')")
    @PutMapping("/products/{id}")
    @OperationLog(entityType = "PRODUCT", action = "UPDATE")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDTO dto) {
        log.info("[产品管理] 更新产品 - ID: {}", id);
        try {
            Product updated = productService.updateProduct(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("[产品管理] 更新产品失败 - ID: {}, {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN') or hasAuthority('AGENT_TYPE_PRODUCTION')")
    @DeleteMapping("/products/{id}")
    @OperationLog(entityType = "PRODUCT", action = "DELETE")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        log.info("[产品管理] 删除产品 - ID: {}", id);
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
        } catch (Exception e) {
            log.error("[产品管理] 删除产品失败 - ID: {}, {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN') or hasAuthority('AGENT_TYPE_PRODUCTION')")
    @DeleteMapping("/products/{id}/hard")
    @OperationLog(entityType = "PRODUCT", action = "HARD_DELETE")
    public ResponseEntity<?> hardDeleteProduct(@PathVariable Long id) {
        log.info("[产品管理] 物理删除产品 - ID: {}", id);
        try {
            productService.hardDeleteProduct(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "物理删除成功"));
        } catch (Exception e) {
            log.error("[产品管理] 物理删除产品失败 - ID: {}, {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> listProducts(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(productService.searchProducts(keyword.trim()));
        }
        return ResponseEntity.ok(productService.listAllProducts());
    }

    @GetMapping("/products/select")
    public ResponseEntity<?> selectProducts(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false, defaultValue = "consumer") String role) {
        log.info("[产品选择] 查询产品 - 关键词: {}, 角色: {}", keyword, role);
        try {
            if (keyword != null && !keyword.trim().isEmpty()) {
                return ResponseEntity.ok(productService.searchProducts(keyword.trim()));
            }
            return ResponseEntity.ok(productService.listAllProducts());
        } catch (Exception e) {
            log.error("[产品选择] 查询失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @PostMapping("/insert/products/list")
    public ResponseEntity<?> getProductsForInsert() {
        log.info("[数据导入] 获取产品列表");
        try {
            return ResponseEntity.ok(productService.listAllProducts());
        } catch (Exception e) {
            log.error("[数据导入] 获取产品列表失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getProductById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    // ============ 防伪码 ============

    @PreAuthorize("hasAnyRole('SUPER_ADMIN') or hasAuthority('AGENT_TYPE_PRODUCTION')")
    @PostMapping("/batches/{id}/security-codes")
    @OperationLog(entityType = "SECURITY_CODE", action = "CREATE")
    public ResponseEntity<?> generateSecurityCodes(@PathVariable Long id, @RequestBody GenerateSecurityCodeRequest request) {
        log.info("[防伪码管理] 生成防伪码 - 批次ID: {}, 数量: {}", id, request.getQuantity());
        try {
            SecurityCodeGenerateResponse response = securityCodeService.generateCodes(id, request.getQuantity());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[防伪码管理] 生成防伪码失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN') or hasAuthority('AGENT_TYPE_PRODUCTION')")
    @GetMapping("/batches/{id}/security-codes")
    public ResponseEntity<?> listSecurityCodes(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(securityCodeService.getCodesByBatchId(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN') or hasAuthority('AGENT_TYPE_PRODUCTION')")
    @GetMapping("/security-codes/export/{batchId}")
    public ResponseEntity<?> exportSecurityCodes(@PathVariable Long batchId) {
        try {
            return ResponseEntity.ok(securityCodeService.exportCodes(batchId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }
}
