package com.foodtraceability.traceability.application.service;

import com.foodtraceability.entity.BatchMaterialRelation;
import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.entity.TraceabilityLink;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.BatchMaterialRelationRepository;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.repository.TraceabilityLinkRepository;
import com.foodtraceability.traceability.application.dto.CreateBatchRequest;
import com.foodtraceability.traceability.application.dto.CreateBatchResponse;
import com.foodtraceability.traceability.interfaces.dto.BatchSelectOption;
import com.foodtraceability.traceability.domain.event.BatchCreated;
import com.foodtraceability.traceability.domain.event.BatchSoftDeleted;
import com.foodtraceability.traceability.domain.service.BatchCreationValidator;
import com.foodtraceability.traceability.domain.vo.BatchNumber;
import com.foodtraceability.traceability.domain.vo.Quantity;
import com.foodtraceability.traceability.infrastructure.messaging.DomainEventPublisherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class ProductionBatchApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ProductionBatchApplicationService.class);

    private final ProductionBatchRepository batchRepo;
    private final ProductRepository productRepo;
    private final BatchMaterialRelationRepository relationRepo;
    private final TraceabilityLinkRepository linkRepo;
    private final BatchCreationValidator validator;
    private final DomainEventPublisherImpl eventPublisher;

    public ProductionBatchApplicationService(ProductionBatchRepository batchRepo,
                                             ProductRepository productRepo,
                                             BatchMaterialRelationRepository relationRepo,
                                             TraceabilityLinkRepository linkRepo,
                                             DomainEventPublisherImpl eventPublisher) {
        this.batchRepo = batchRepo;
        this.productRepo = productRepo;
        this.relationRepo = relationRepo;
        this.linkRepo = linkRepo;
        this.validator = new BatchCreationValidator();
        this.eventPublisher = eventPublisher;
    }

    private long nextBatchSeq() {
        return batchRepo.findByIsDeletedFalse().stream()
                .map(ProductionBatch::getBatchNumber)
                .filter(n -> n != null && n.matches("B\\d{8}\\d{4}"))
                .map(n -> n.substring(9))
                .mapToLong(Long::parseLong)
                .max()
                .orElse(0) + 1;
    }

    public CreateBatchResponse createBatch(CreateBatchRequest req) {
        var product = productRepo.findById(req.productId())
                .orElseThrow(() -> new BusinessException("产品不存在"));
        validator.validateProduct(product);
        validator.validateMaterialsNotEmpty(req.materialPurchaseIds());

        String shelfLife = req.shelfLife() != null ? req.shelfLife() : product.getShelfLife();
        validator.validateShelfLife(shelfLife);

        BatchNumber batchNo = BatchNumber.generate(
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                nextBatchSeq());
        Quantity qty = Quantity.of(req.quantity() != null ? req.quantity() : 0.0,
                req.unit() != null ? req.unit() : "");

        ProductionBatch batch = ProductionBatch.create(
                batchNo, req.productId(), req.productionDate(), shelfLife, qty);
        batch = batchRepo.save(batch);

        Long batchId = batch.getId();
        for (Long mpId : req.materialPurchaseIds()) {
            relationRepo.save(BatchMaterialRelation.create(batchId, mpId));
            if (!linkRepo.existsByBatchIdAndEntityTypeAndEntityId(batchId, "MATERIAL_PURCHASE", mpId)) {
                linkRepo.save(TraceabilityLink.create(batchId, "MATERIAL_PURCHASE", mpId));
            }
        }

        var event = new BatchCreated(batchId, batchNo, req.productId(), req.materialPurchaseIds());
        eventPublisher.publish(event);

        log.info("Batch created: {} (productId={})", batchNo, req.productId());
        return new CreateBatchResponse(batchId, batchNo.value(), product.getName());
    }

    @Transactional(readOnly = true)
    public List<ProductionBatch> listBatches(Long productId) {
        List<ProductionBatch> batches = batchRepo.findByIsDeletedFalse();
        if (productId != null) {
            batches = batches.stream()
                    .filter(b -> b.getProductId().equals(productId))
                    .toList();
        }
        return batches;
    }

    @Transactional(readOnly = true)
    public ProductionBatch getBatch(Long id) {
        return batchRepo.findById(id)
                .orElseThrow(() -> new BusinessException("批次不存在: " + id));
    }

    @Transactional(readOnly = true)
    public ProductionBatch getBatchByBatchNumber(String batchNumber) {
        return batchRepo.findByBatchNumberAndIsDeletedFalse(batchNumber)
                .orElseThrow(() -> new BusinessException("批次不存在: " + batchNumber));
    }

    @Transactional(readOnly = true)
    public List<BatchSelectOption> getSelectOptions(String keyword) {
        List<ProductionBatch> batches = batchRepo.findByIsDeletedFalse();
        return batches.stream()
                .map(b -> new BatchSelectOption(b.getId(), b.getBatchNumber(),
                        b.getProductId(),
                        b.getProduct() != null ? b.getProduct().getName() : null))
                .toList();
    }

    public void deleteBatch(Long id) {
        ProductionBatch batch = batchRepo.findById(id)
                .orElseThrow(() -> new BusinessException("批次不存在: " + id));
        batch.softDelete();
        batchRepo.save(batch);

        eventPublisher.publish(new BatchSoftDeleted(id));
        log.info("Batch soft-deleted: id={}", id);
    }

}
