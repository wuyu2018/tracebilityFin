package com.foodtraceability.traceability.interfaces.rest;

import com.foodtraceability.traceability.application.service.TraceabilityQueryApplicationService;
import com.foodtraceability.traceability.application.service.TraceabilityQueryApplicationService.TraceResult;
import com.foodtraceability.traceability.interfaces.dto.TraceabilityQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/trace")
public class TraceabilityQueryController {

    private static final Logger log = LoggerFactory.getLogger(TraceabilityQueryController.class);

    private final TraceabilityQueryApplicationService queryService;

    public TraceabilityQueryController(TraceabilityQueryApplicationService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String code) {
        log.info("[v2 追溯] 防伪码查询: {}", maskCode(code));
        try {
            TraceResult result = queryService.queryByCode(code);
            TraceabilityQueryResponse response = toResponse(result);
            boolean repeated = Boolean.TRUE.equals(result.isRepeatedQuery());
            if (repeated) {
                log.warn("[v2 追溯] 重复查询: {}", maskCode(code));
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "data", response,
                        "message", "该产品已被查询过 " + (result.scanCount() - 1) + " 次，首次查询时间："
                                + result.firstScanTime() + "，该产品可能是伪品，请谨慎购买！"
                ));
            }
            return ResponseEntity.ok(Map.of("valid", true, "data", response));
        } catch (Exception e) {
            log.error("[v2 追溯] 查询失败: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("valid", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/batch/{batchNumber}")
    public ResponseEntity<?> traceByBatch(@PathVariable String batchNumber) {
        log.info("[v2 追溯] 批次查询: {}", batchNumber);
        try {
            TraceResult result = queryService.queryByBatchNumber(batchNumber);
            TraceabilityQueryResponse response = toResponse(result);
            return ResponseEntity.ok(Map.of("valid", true, "data", response));
        } catch (Exception e) {
            log.error("[v2 追溯] 批次查询失败: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("valid", false, "message", e.getMessage()));
        }
    }

    private TraceabilityQueryResponse toResponse(TraceResult r) {
        return new TraceabilityQueryResponse(
                r.product() != null ? new TraceabilityQueryResponse.ProductInfo(
                        r.product().id(), r.product().name(), r.product().specification(),
                        r.product().shelfLife(), r.product().imageUrl(),
                        r.product().contactPhone(), r.product().contactEmail()) : null,
                r.batch() != null ? new TraceabilityQueryResponse.BatchInfo(
                        r.batch().id(), r.batch().batchNumber(), r.batch().productionDate(),
                        r.batch().shelfLife(), r.batch().createdAt()) : null,
                r.materials().stream()
                        .map(m -> new TraceabilityQueryResponse.MaterialInfo(
                                m.materialName(), m.batchNumber(), m.supplierName(), m.producerName(),
                                m.producerAddress(),
                                m.purchaseDate() != null ? m.purchaseDate().toString() : null))
                        .toList(),
                r.inspection() != null ? new TraceabilityQueryResponse.InspectionInfo(
                        r.inspection().sampleName(), r.inspection().sampleQuantity(),
                        r.inspection().sampleSpecification(), r.inspection().imageUrl(),
                        r.inspection().inspectorName(),
                        r.inspection().inspectionTime() != null
                                ? r.inspection().inspectionTime().toString() : null,
                        r.inspection().resultStatus(),
                        r.inspection().resultDetail()) : null,
                r.storage() != null ? new TraceabilityQueryResponse.StorageInfo(
                        r.storage().storageTime(), r.storage().outboundTime(),
                        r.storage().warehouseLocation()) : null,
                r.transportSale() != null ? new TraceabilityQueryResponse.TransportSaleInfo(
                        r.transportSale().time(),
                        r.transportSale().salesRegion(),
                        r.transportSale().transportCompany()) : null,
                r.status(),
                r.isRepeatedQuery(),
                r.scanCount(),
                r.firstScanTime(),
                buildQueryTip(r)
        );
    }

    private String buildQueryTip(TraceResult r) {
        if (!Boolean.TRUE.equals(r.isRepeatedQuery())) return null;
        return String.format("该产品已被查询过 %d 次，首次查询时间：%s。重复查询可能是伪品，请谨慎购买！",
                r.scanCount() - 1, r.firstScanTime());
    }

    private String maskCode(String code) {
        if (code == null || code.length() <= 8) return "***";
        return code.substring(0, 4) + "****" + code.substring(code.length() - 4);
    }
}
