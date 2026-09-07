package com.foodtraceability.traceability.application.service;

import com.foodtraceability.entity.Material;
import com.foodtraceability.entity.MaterialPurchase;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.MaterialPurchaseRepository;
import com.foodtraceability.repository.MaterialRepository;
import com.foodtraceability.traceability.application.dto.CreateMaterialPurchaseRequest;
import com.foodtraceability.traceability.application.dto.MaterialPurchaseResponse;
import com.foodtraceability.traceability.application.dto.UpdateMaterialPurchaseRequest;
import com.foodtraceability.traceability.domain.event.MaterialPurchaseChanged;
import com.foodtraceability.traceability.infrastructure.messaging.DomainEventPublisherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MaterialPurchaseApplicationService {

    private static final Logger log = LoggerFactory.getLogger(MaterialPurchaseApplicationService.class);

    private final MaterialPurchaseRepository repository;
    private final MaterialRepository materialRepository;
    private final DomainEventPublisherImpl eventPublisher;

    public MaterialPurchaseApplicationService(MaterialPurchaseRepository repository,
                                               MaterialRepository materialRepository,
                                               DomainEventPublisherImpl eventPublisher) {
        this.repository = repository;
        this.materialRepository = materialRepository;
        this.eventPublisher = eventPublisher;
    }

    public MaterialPurchaseResponse createMaterialPurchase(CreateMaterialPurchaseRequest req) {
        Material material = materialRepository.findById(req.materialId())
                .orElseThrow(() -> new BusinessException("原料品种不存在: " + req.materialId()));

        MaterialPurchase entity = MaterialPurchase.recordPurchase(
                material, req.batchNumber(), req.supplierName(),
                req.producerName(), req.producerAddress(),
                req.purchaseDate(), req.quantity(), req.unit());
        entity = repository.save(entity);

        eventPublisher.publish(new MaterialPurchaseChanged(entity.getId(), "CREATE"));
        log.info("[V2 采购单] 创建 - ID: {}, 物料: {}", entity.getId(), material.getName());
        return toResponse(entity);
    }

    public MaterialPurchaseResponse updateMaterialPurchase(Long id, UpdateMaterialPurchaseRequest req) {
        MaterialPurchase entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("采购单不存在: " + id));
        if (entity.isDeleted()) {
            throw new BusinessException("采购单已删除，禁止修改: " + id);
        }

        entity.updatePurchaseDetails(
                req.batchNumber(), req.supplierName(),
                req.producerName(), req.producerAddress(),
                req.purchaseDate(), req.quantity(), req.unit());
        entity = repository.save(entity);

        eventPublisher.publish(new MaterialPurchaseChanged(entity.getId(), "UPDATE"));
        log.info("[V2 采购单] 更新 - ID: {}", entity.getId());
        return toResponse(entity);
    }

    public void deleteMaterialPurchase(Long id) {
        MaterialPurchase entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("采购单不存在: " + id));
        entity.softDelete();
        repository.save(entity);

        eventPublisher.publish(new MaterialPurchaseChanged(entity.getId(), "SOFT_DELETE"));
        log.info("[V2 采购单] 软删除 - ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<MaterialPurchaseResponse> listMaterialPurchases(Long materialId) {
        List<MaterialPurchase> purchases;
        if (materialId != null) {
            purchases = repository.findByMaterialIdAndIsDeletedFalse(materialId);
        } else {
            purchases = repository.findByIsDeletedFalse();
        }
        return purchases.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MaterialPurchaseResponse getMaterialPurchase(Long id) {
        MaterialPurchase entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("采购单不存在: " + id));
        return toResponse(entity);
    }

    private MaterialPurchaseResponse toResponse(MaterialPurchase entity) {
        return new MaterialPurchaseResponse(
                entity.getId(),
                entity.getMaterial().getId(),
                entity.getMaterialName(),
                entity.getBatchNumber(),
                entity.getSupplierName(),
                entity.getProducerName(),
                entity.getProducerAddress(),
                entity.getPurchaseDate(),
                entity.getQuantity(),
                entity.getUnit(),
                entity.isDeleted());
    }

}
