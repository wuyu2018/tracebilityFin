package com.foodtraceability.traceability.interfaces.rest;

import com.foodtraceability.traceability.application.service.TransportSaleApplicationService;
import com.foodtraceability.traceability.interfaces.dto.RecordTransportSaleRequest;
import com.foodtraceability.traceability.interfaces.dto.TransportSaleResponse;
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
@PreAuthorize("hasAnyRole('SUPER_ADMIN') or hasAnyAuthority('AGENT_TYPE_CIRCULATION', 'AGENT_TYPE_SALES')")
public class TransportSaleController {

    private static final Logger log = LoggerFactory.getLogger(TransportSaleController.class);

    private final TransportSaleApplicationService appService;
    private final SecurityUtils securityUtils;

    public TransportSaleController(TransportSaleApplicationService appService, SecurityUtils securityUtils) {
        this.appService = appService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/transport-sales")
    public ResponseEntity<?> listTransportSales() {
        try {
            return ResponseEntity.ok(appService.listTransportSales());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/transport-sales")
    public ResponseEntity<?> recordTransportSale(@RequestBody RecordTransportSaleRequest req) {
        log.info("[v2] 录入运输销售 batchId={}", req.getBatchId());
        try {
            var result = appService.recordTransportSale(req.toAppRequest());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new TransportSaleResponse(result.id(), result.batchId(),
                            result.transportCompany(), result.salesRegion()));
        } catch (Exception e) {
            log.error("[v2] 录入运输销售失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
