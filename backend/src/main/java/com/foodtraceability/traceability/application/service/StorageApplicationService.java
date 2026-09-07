package com.foodtraceability.traceability.application.service;

import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.entity.Storage;
import com.foodtraceability.entity.TraceabilityLink;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.repository.StorageRepository;
import com.foodtraceability.repository.TraceabilityLinkRepository;
import com.foodtraceability.traceability.application.dto.RecordStorageRequest;
import com.foodtraceability.traceability.application.dto.RecordStorageResponse;
import com.foodtraceability.traceability.domain.event.GoodsReceived;
import com.foodtraceability.traceability.infrastructure.messaging.DomainEventPublisherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class StorageApplicationService {

    private static final Logger log = LoggerFactory.getLogger(StorageApplicationService.class);

    private final StorageRepository storageRepo;
    private final ProductionBatchRepository batchRepo;
    private final TraceabilityLinkRepository linkRepo;
    private final DomainEventPublisherImpl eventPublisher;

    public record StorageListResponse(Long id, Long batchId, LocalDateTime storageTime,
                                       LocalDateTime outboundTime, Double quantity,
                                       String unit, String warehouseLocation,
                                       String batchNumber, String productName) {}

    public StorageApplicationService(StorageRepository storageRepo,
                                     ProductionBatchRepository batchRepo,
                                     TraceabilityLinkRepository linkRepo,
                                     DomainEventPublisherImpl eventPublisher) {
        this.storageRepo = storageRepo;
        this.batchRepo = batchRepo;
        this.linkRepo = linkRepo;
        this.eventPublisher = eventPublisher;
    }

    public RecordStorageResponse recordStorage(RecordStorageRequest req) {
        if (req.batchId() == null) {
            throw new BusinessException("批次不能为空");
        }
        ProductionBatch batch = batchRepo.findById(req.batchId())
                .orElseThrow(() -> new BusinessException("批次不存在: " + req.batchId()));

        Storage storage = Storage.create(
                req.batchId(), req.storageTime(), req.outboundTime(), req.quantity(), req.unit(), req.warehouseLocation());
        storage = storageRepo.save(storage);

        Long storageId = storage.getId();
        batch.setStorageId(storageId);
        batchRepo.save(batch);

        if (!linkRepo.existsByBatchIdAndEntityTypeAndEntityId(req.batchId(), "STORAGE", storageId)) {
            linkRepo.save(TraceabilityLink.create(req.batchId(), "STORAGE", storageId));
        }

        var event = new GoodsReceived(storageId, req.batchId(), req.storageTime());
        eventPublisher.publish(event);

        log.info("Storage recorded: id={}, batchId={}", storageId, req.batchId());
        return new RecordStorageResponse(storageId, req.batchId(), req.warehouseLocation());
    }

    @Transactional(readOnly = true)
    public List<StorageListResponse> listStorages() {
        List<Storage> storages = storageRepo.findAll();
        Set<Long> batchIds = storages.stream().map(Storage::getBatchId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, ProductionBatch> batchMap = batchRepo.findAllById(batchIds).stream()
                .collect(Collectors.toMap(ProductionBatch::getId, b -> b));
        return storages.stream()
                .map(s -> {
                    ProductionBatch b = batchMap.get(s.getBatchId());
                    String bn = b != null ? b.getBatchNumber() : null;
                    String pn = b != null && b.getProduct() != null ? b.getProduct().getName() : null;
                    return new StorageListResponse(s.getId(), s.getBatchId(), s.getStorageTime(),
                            s.getOutboundTime(), s.getQuantity(), s.getUnit(), s.getWarehouseLocation(),
                            bn, pn);
                })
                .toList();
    }
}