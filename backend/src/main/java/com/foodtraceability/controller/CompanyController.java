package com.foodtraceability.controller;

import com.foodtraceability.aop.OperationLog;
import com.foodtraceability.service.CompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/companies")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class CompanyController {

    private static final Logger log = LoggerFactory.getLogger(CompanyController.class);

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<?> listCompanies() {
        try {
            return ResponseEntity.ok(companyService.listAllCompanies());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCompany(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(companyService.getCompany(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    @OperationLog(entityType = "COMPANY", action = "CREATE")
    public ResponseEntity<?> createCompany(@RequestBody Map<String, String> body) {
        log.info("[公司管理] 创建公司: {}", body.get("name"));
        try {
            var company = companyService.createCompany(
                    body.get("name"),
                    body.get("contactPhone"),
                    body.get("address")
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(company);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @OperationLog(entityType = "COMPANY", action = "UPDATE")
    public ResponseEntity<?> updateCompany(@PathVariable Long id, @RequestBody Map<String, String> body) {
        log.info("[公司管理] 更新公司: {}", id);
        try {
            var company = companyService.updateCompany(
                    id,
                    body.get("name"),
                    body.get("contactPhone"),
                    body.get("address")
            );
            return ResponseEntity.ok(company);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @OperationLog(entityType = "COMPANY", action = "DELETE")
    public ResponseEntity<?> deleteCompany(@PathVariable Long id) {
        log.info("[公司管理] 删除公司: {}", id);
        try {
            companyService.deleteCompany(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
