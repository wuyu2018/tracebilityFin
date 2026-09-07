package com.foodtraceability.traceability.application.service;

import com.foodtraceability.dto.BatchDetailDto;
import com.foodtraceability.entity.*;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TraceabilityQueryApplicationService {

    private static final Logger log = LoggerFactory.getLogger(TraceabilityQueryApplicationService.class);

    private final SecurityCodeRepository securityCodeRepo;
    private final ProductionBatchRepository batchRepo;
    private final ProductRepository productRepo;
    private final BatchMaterialRelationRepository relationRepo;
    private final MaterialPurchaseRepository materialPurchaseRepo;
    private final MaterialRepository materialRepo;
    private final InspectionRepository inspectionRepo;
    private final StorageRepository storageRepo;
    private final TransportSaleRepository transportSaleRepo;
    private final TraceabilityLinkRepository linkRepo;

    public TraceabilityQueryApplicationService(SecurityCodeRepository securityCodeRepo,
                                                ProductionBatchRepository batchRepo,
                                                ProductRepository productRepo,
                                                BatchMaterialRelationRepository relationRepo,
                                                MaterialPurchaseRepository materialPurchaseRepo,
                                                MaterialRepository materialRepo,
                                                InspectionRepository inspectionRepo,
                                                StorageRepository storageRepo,
                                                TransportSaleRepository transportSaleRepo,
                                                TraceabilityLinkRepository linkRepo) {
        this.securityCodeRepo = securityCodeRepo;
        this.batchRepo = batchRepo;
        this.productRepo = productRepo;
        this.relationRepo = relationRepo;
        this.materialPurchaseRepo = materialPurchaseRepo;
        this.materialRepo = materialRepo;
        this.inspectionRepo = inspectionRepo;
        this.storageRepo = storageRepo;
        this.transportSaleRepo = transportSaleRepo;
        this.linkRepo = linkRepo;
    }

    @Transactional
    public TraceResult queryByCode(String code) {
        SecurityCode sc = securityCodeRepo.findByCode(code)
                .orElseThrow(() -> new BusinessException("防伪码不存在: " + code));

        sc.recordQueryAndActivateIfNeeded();
        securityCodeRepo.save(sc);

        ProductionBatch batch = batchRepo.findById(sc.getBatch().getId())
                .orElseThrow(() -> new BusinessException("批次不存在"));

        return buildTraceResult(sc, batch);
    }

    @Transactional(readOnly = true)
    public TraceResult queryByBatchNumber(String batchNumber) {
        ProductionBatch batch = batchRepo.findByBatchNumberAndIsDeletedFalse(batchNumber)
                .orElseThrow(() -> new BusinessException("批次不存在: " + batchNumber));

        return buildTraceResult(null, batch);
    }

    @Transactional(readOnly = true)
    public BatchDetailDto getBatchDetail(Long batchId) {
        ProductionBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new BusinessException("批次不存在: " + batchId));

        BatchDetailDto dto = new BatchDetailDto();

        dto.setBatch(new BatchDetailDto.BatchInfo(
                batch.getId(), batch.getBatchNumber(), batch.getProductionDate(),
                batch.getShelfLife(), batch.getQuantity(), batch.getUnit(),
                batch.getCreatedAt()));

        Product product = batch.getProduct();
        if (product == null && batch.getProductId() != null) {
            product = productRepo.findById(batch.getProductId()).orElse(null);
        }
        if (product != null) {
            dto.setProduct(new BatchDetailDto.ProductInfo(
                    product.getId(), product.getName(), product.getSpecification(),
                    product.getShelfLife(), product.getContactPhone(), product.getContactEmail()));
        }

        Map<String, List<Long>> links = groupLinksByEntityType(batchId);

        List<Long> mpIds = getEntityIds(links, "MATERIAL_PURCHASE");
        if (mpIds == null) {
            mpIds = buildMaterialIdsLegacy(batchId);
        }
        List<BatchDetailDto.MaterialInfo> materials = new ArrayList<>();
        for (Long mpId : mpIds) {
            MaterialPurchase mp = materialPurchaseRepo.findById(mpId).orElse(null);
            if (mp == null) continue;
            String materialName = mp.getMaterial() != null ? mp.getMaterial().getName() : null;
            materials.add(new BatchDetailDto.MaterialInfo(
                    materialName, mp.getBatchNumber(), mp.getSupplierName(),
                    mp.getProducerName(), mp.getProducerAddress(), mp.getPurchaseDate(),
                    mp.getQuantity(), mp.getUnit()));
        }
        dto.setMaterials(materials);

        List<Long> inspIds = getEntityIds(links, "INSPECTION");
        if (inspIds != null && !inspIds.isEmpty()) {
            inspectionRepo.findById(inspIds.get(0)).ifPresent(insp -> {
                dto.setInspection(new BatchDetailDto.InspectionInfo(
                        insp.getSampleName(), insp.getSampleQuantity(),
                        insp.getSampleSpecification(), insp.getImageUrl(),
                        insp.getInspectorName(), insp.getInspectionTime(),
                        insp.getResultStatus(), insp.getResultDetail()));
            });
        }

        List<Long> stIds = getEntityIds(links, "STORAGE");
        if (stIds != null && !stIds.isEmpty()) {
            storageRepo.findById(stIds.get(0)).ifPresent(s -> {
                dto.setStorage(new BatchDetailDto.StorageInfo(
                        s.getStorageTime(), s.getOutboundTime(), s.getWarehouseLocation(),
                        s.getQuantity(), s.getUnit()));
            });
        }

        List<Long> trIds = getEntityIds(links, "TRANSPORT_SALE");
        if (trIds != null && !trIds.isEmpty()) {
            transportSaleRepo.findById(trIds.get(0)).ifPresent(ts -> {
                dto.setTransport(new BatchDetailDto.TransportInfo(
                        ts.getTransportCompany(), ts.getVehicleNumber(), ts.getTime(),
                        ts.getSalesRegion(), ts.getReceiverName(), ts.getReceiverContact(),
                        ts.getEnvironmentTemperature(), ts.getProductTemperature(),
                        ts.getRecorderName()));
            });
        }

        Map<String, Long> statusCount = securityCodeRepo.findByBatch_Id(batchId).stream()
                .collect(Collectors.groupingBy(
                        sc -> sc.getStatus() != null ? sc.getStatus() : "未激活",
                        Collectors.counting()));
        long total = statusCount.values().stream().mapToLong(Long::longValue).sum();
        dto.setCodes(new BatchDetailDto.CodeStats(
                total,
                statusCount.getOrDefault("未激活", 0L),
                statusCount.getOrDefault("已激活", 0L),
                statusCount.getOrDefault("已冻结", 0L)));

        log.info("[BatchDetail] batchId={} materials={} inspection={} storage={} transport={} codes={}",
                batchId, materials.size(), dto.getInspection() != null,
                dto.getStorage() != null, dto.getTransport() != null, total);

        return dto;
    }

    private List<Long> buildMaterialIdsLegacy(Long batchId) {
        return relationRepo.findById_BatchId(batchId).stream()
                .map(r -> r.getId().getMaterialPurchaseId())
                .toList();
    }

    private TraceResult buildTraceResult(SecurityCode sc, ProductionBatch batch) {
        Product product = batch.getProduct();
        if (product == null && batch.getProductId() != null) {
            product = productRepo.findById(batch.getProductId()).orElse(null);
        }

        ProductDto productDto = product != null
                ? new ProductDto(product.getId(), product.getName(), product.getSpecification(),
                        product.getShelfLife(), product.getImageUrl(),
                        product.getContactPhone(), product.getContactEmail())
                : null;

        BatchDto batchDto = new BatchDto(batch.getId(), batch.getBatchNumber(),
                batch.getProductionDate(), batch.getShelfLife(), batch.getCreatedAt());

        Map<String, List<Long>> links = groupLinksByEntityType(batch.getId());

        List<MaterialInfo> materials = buildMaterialInfos(batch.getId(), links);

        InspectionDto inspectionDto = buildInspection(batch.getId(), links);

        StorageDto storageDto = buildStorage(batch.getId(), links);

        TransportSaleDto transportSaleDto = buildTransportSale(batch.getId(), links);

        log.info("[TraceQuery] batchId={} materialCount={} inspectionFound={} storageFound={} transportFound={}",
                batch.getId(), materials.size(), inspectionDto != null,
                storageDto != null, transportSaleDto != null);

        String status = sc != null ? sc.getStatus() : "未扫码";
        Boolean isRepeatedQuery = sc != null && sc.isRepeatedQuery();
        int scanCount = sc != null ? sc.getQueryCount() : 0;
        String firstScanTime = sc != null && sc.getFirstScanTime() != null
                ? sc.getFirstScanTime().toString()
                : null;

        return new TraceResult(productDto, batchDto, materials, inspectionDto, storageDto,
                transportSaleDto, status, isRepeatedQuery, scanCount, firstScanTime);
    }

    private Map<String, List<Long>> groupLinksByEntityType(Long batchId) {
        List<TraceabilityLink> links = linkRepo.findByBatchId(batchId);
        if (links == null || links.isEmpty()) {
            return Map.of();
        }
        return links.stream()
                .collect(Collectors.groupingBy(
                        TraceabilityLink::getEntityType,
                        Collectors.mapping(TraceabilityLink::getEntityId, Collectors.toList())));
    }

    private List<Long> getEntityIds(Map<String, List<Long>> links, String entityType) {
        List<Long> ids = links.getOrDefault(entityType, List.of());
        return ids.isEmpty() ? null : ids;
    }

    private List<MaterialInfo> buildMaterialInfos(Long batchId, Map<String, List<Long>> links) {
        List<Long> materialIds = getEntityIds(links, "MATERIAL_PURCHASE");

        if (materialIds == null) {
            return buildMaterialInfosLegacy(batchId);
        }

        return materialIds.stream()
                .map(id -> {
                    MaterialPurchase mp = materialPurchaseRepo.findById(id).orElse(null);
                    if (mp == null) return new MaterialInfo(null, null, null, null, null, null);
                    String materialName = mp.getMaterial() != null ? mp.getMaterial().getName() : null;
                    return new MaterialInfo(materialName, mp.getBatchNumber(),
                            mp.getSupplierName(), mp.getProducerName(),
                            mp.getProducerAddress(), mp.getPurchaseDate());
                })
                .toList();
    }

    private InspectionDto buildInspection(Long batchId, Map<String, List<Long>> links) {
        List<Long> inspectionIds = getEntityIds(links, "INSPECTION");

        if (inspectionIds == null) {
            List<Inspection> inspections = inspectionRepo.findByBatch_Id(batchId);
            Inspection inspection = inspections.isEmpty() ? null : inspections.get(0);
            return inspection != null ? inspectionToDto(inspection) : null;
        }

        return inspectionRepo.findById(inspectionIds.get(0))
                .map(this::inspectionToDto)
                .orElse(null);
    }

    private InspectionDto inspectionToDto(Inspection inspection) {
        return new InspectionDto(inspection.getSampleName(), inspection.getSampleQuantity(),
                inspection.getSampleSpecification(), inspection.getImageUrl(),
                inspection.getInspectorName(), inspection.getInspectionTime(),
                inspection.getResultStatus(), inspection.getResultDetail());
    }

    private StorageDto buildStorage(Long batchId, Map<String, List<Long>> links) {
        List<Long> storageIds = getEntityIds(links, "STORAGE");

        if (storageIds == null) {
            Storage storage = batchRepo.findById(batchId)
                    .map(b -> b.getStorageId() != null
                            ? storageRepo.findById(b.getStorageId()).orElse(null)
                            : null)
                    .orElse(null);
            return storage != null ? storageToDto(storage) : null;
        }

        return storageRepo.findById(storageIds.get(0))
                .map(this::storageToDto)
                .orElse(null);
    }

    private StorageDto storageToDto(Storage s) {
        return new StorageDto(s.getStorageTime(), s.getOutboundTime(), s.getWarehouseLocation());
    }

    private TransportSaleDto buildTransportSale(Long batchId, Map<String, List<Long>> links) {
        List<Long> transportIds = getEntityIds(links, "TRANSPORT_SALE");

        if (transportIds == null) {
            TransportSale ts = batchRepo.findById(batchId)
                    .map(b -> b.getTransportSaleId() != null
                            ? transportSaleRepo.findById(b.getTransportSaleId()).orElse(null)
                            : null)
                    .orElse(null);
            return ts != null ? transportSaleToDto(ts) : null;
        }

        return transportSaleRepo.findById(transportIds.get(0))
                .map(this::transportSaleToDto)
                .orElse(null);
    }

    private TransportSaleDto transportSaleToDto(TransportSale ts) {
        return new TransportSaleDto(ts.getTime(), ts.getSalesRegion(), ts.getTransportCompany());
    }

    private List<MaterialInfo> buildMaterialInfosLegacy(Long batchId) {
        List<BatchMaterialRelation> relations = relationRepo.findById_BatchId(batchId);
        return relations.stream()
                .map(r -> {
                    MaterialPurchase mp = materialPurchaseRepo
                            .findById(r.getId().getMaterialPurchaseId()).orElse(null);
                    if (mp == null) return new MaterialInfo(null, null, null, null, null, null);
                    String materialName = mp.getMaterial() != null ? mp.getMaterial().getName() : null;
                    return new MaterialInfo(materialName, mp.getBatchNumber(),
                            mp.getSupplierName(), mp.getProducerName(),
                            mp.getProducerAddress(), mp.getPurchaseDate());
                })
                .toList();
    }

    public record ProductDto(Long id, String name, String specification,
                              String shelfLife, String imageUrl,
                              String contactPhone, String contactEmail) {}

    public record BatchDto(Long id, String batchNumber, LocalDate productionDate,
                            String shelfLife, LocalDateTime createdAt) {}

    public record InspectionDto(String sampleName, Integer sampleQuantity,
                                 String sampleSpecification, String imageUrl,
                                 String inspectorName, LocalDateTime inspectionTime,
                                 String resultStatus, String resultDetail) {}

    public record StorageDto(LocalDateTime storageTime, LocalDateTime outboundTime,
                              String warehouseLocation) {}

    public record TransportSaleDto(LocalDateTime time, String salesRegion,
                                    String transportCompany) {}

    public record MaterialInfo(String materialName, String batchNumber,
                                String supplierName, String producerName,
                                String producerAddress, LocalDateTime purchaseDate) {}

    public record TraceResult(ProductDto product, BatchDto batch,
                                List<MaterialInfo> materials,
                                InspectionDto inspection, StorageDto storage,
                                TransportSaleDto transportSale,
                                String status, Boolean isRepeatedQuery,
                                int scanCount, String firstScanTime) {}
}
