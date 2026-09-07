package com.foodtraceability.traceability.application.service;

import com.foodtraceability.entity.Inspection;
import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.entity.TraceabilityLink;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.InspectionRepository;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.repository.TraceabilityLinkRepository;
import com.foodtraceability.traceability.application.dto.CompleteInspectionRequest;
import com.foodtraceability.traceability.application.dto.CompleteInspectionResponse;
import com.foodtraceability.traceability.domain.vo.InspectionResult;
import com.foodtraceability.traceability.domain.event.DomainEvent;
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
public class InspectionApplicationService {

    private static final Logger log = LoggerFactory.getLogger(InspectionApplicationService.class);

    private final InspectionRepository inspectionRepo;
    private final ProductionBatchRepository batchRepo;
    private final TraceabilityLinkRepository linkRepo;
    private final DomainEventPublisherImpl eventPublisher;

    public record InspectionListResponse(Long id, Long batchId, String sampleName,
                                          Integer sampleQuantity, String sampleSpecification,
                                          String imageUrl, String resultStatus, String resultDetail,
                                          String inspectorName, LocalDateTime inspectionTime,
                                          String batchNumber, String productName) {}

    public InspectionApplicationService(InspectionRepository inspectionRepo,
                                        ProductionBatchRepository batchRepo,
                                        TraceabilityLinkRepository linkRepo,
                                        DomainEventPublisherImpl eventPublisher) {
        this.inspectionRepo = inspectionRepo;
        this.batchRepo = batchRepo;
        this.linkRepo = linkRepo;
        this.eventPublisher = eventPublisher;
    }

    public CompleteInspectionResponse completeInspection(CompleteInspectionRequest req) {
        if (req.batchId() == null) {
            throw new BusinessException("批次不能为空");
        }
        ProductionBatch batch = batchRepo.findById(req.batchId())
                .orElseThrow(() -> new BusinessException("批次不存在: " + req.batchId()));

        Inspection inspection = Inspection.create(
                req.batchId(), req.sampleName(), req.sampleQuantity(),
                req.sampleSpecification());
        if (req.imageUrl() != null) {
            inspection.setImageUrl(req.imageUrl());
        }

        inspection = inspectionRepo.save(inspection);

        InspectionResult result = req.qualified()
                ? InspectionResult.pass()
                : InspectionResult.fail(req.failReason());
        inspection.complete(result, req.inspectorName());
        inspection = inspectionRepo.save(inspection);

        if (!linkRepo.existsByBatchIdAndEntityTypeAndEntityId(req.batchId(), "INSPECTION", inspection.getId())) {
            linkRepo.save(TraceabilityLink.create(req.batchId(), "INSPECTION", inspection.getId()));
        }

        DomainEvent event = inspection.pullEvents().get(0);
        eventPublisher.publish(event);

        log.info("Inspection completed: id={}, batchId={}, result={}",
                inspection.getId(), req.batchId(), result.displayStatus());
        return new CompleteInspectionResponse(inspection.getId(), req.batchId(), result.displayStatus());
    }

    @Transactional(readOnly = true)
    public List<InspectionListResponse> listInspections() {
        List<Inspection> inspections = inspectionRepo.findAll();
        Set<Long> batchIds = inspections.stream().map(Inspection::getBatchId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, ProductionBatch> batchMap = batchRepo.findAllById(batchIds).stream()
                .collect(Collectors.toMap(ProductionBatch::getId, b -> b));
        return inspections.stream()
                .map(i -> {
                    ProductionBatch b = batchMap.get(i.getBatchId());
                    String bn = b != null ? b.getBatchNumber() : null;
                    String pn = b != null && b.getProduct() != null ? b.getProduct().getName() : null;
                    return new InspectionListResponse(i.getId(), i.getBatchId(), i.getSampleName(),
                            i.getSampleQuantity(), i.getSampleSpecification(), i.getImageUrl(),
                            i.getResultStatus(), i.getResultDetail(), i.getInspectorName(),
                            i.getInspectionTime(), bn, pn);
                })
                .toList();
    }
}