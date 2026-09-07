package com.foodtraceability.traceability.application.service;

import com.foodtraceability.entity.Material;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.MaterialRepository;
import com.foodtraceability.traceability.application.dto.CreateMaterialRequest;
import com.foodtraceability.traceability.application.dto.MaterialResponse;
import com.foodtraceability.traceability.application.dto.UpdateMaterialRequest;
import com.foodtraceability.traceability.domain.event.MaterialChanged;
import com.foodtraceability.traceability.infrastructure.messaging.DomainEventPublisherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MaterialApplicationService {

    private static final Logger log = LoggerFactory.getLogger(MaterialApplicationService.class);

    private final MaterialRepository repository;
    private final DomainEventPublisherImpl eventPublisher;

    public MaterialApplicationService(MaterialRepository repository, DomainEventPublisherImpl eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public MaterialResponse createMaterial(CreateMaterialRequest req) {
        if (repository.existsByName(req.name())) {
            throw new BusinessException("原料品种已存在: " + req.name());
        }
        Material entity = new Material();
        entity.setName(req.name());
        entity.setIsActive(true);
        entity = repository.save(entity);

        eventPublisher.publish(new MaterialChanged(entity.getId(), "CREATE"));
        log.info("[V2 物料品种] 创建 - ID: {}, 名称: {}", entity.getId(), entity.getName());
        return toResponse(entity);
    }

    public MaterialResponse updateMaterial(Long id, UpdateMaterialRequest req) {
        Material entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("原料品种不存在: " + id));
        if (!entity.isActive()) {
            throw new BusinessException("原料品种已停用，禁止修改: " + id);
        }

        String action = "UPDATE";
        if (req.name() != null) {
            entity.changeName(req.name());
        }
        if (req.isActive() != null) {
            if (req.isActive() && !entity.isActive()) {
                action = "ACTIVATE";
            } else if (!req.isActive() && entity.isActive()) {
                action = "DEACTIVATE";
            }
            entity.setActiveStatus(req.isActive());
        }
        entity = repository.save(entity);

        eventPublisher.publish(new MaterialChanged(entity.getId(), action));
        log.info("[V2 物料品种] 更新 - ID: {}, action: {}", entity.getId(), action);
        return toResponse(entity);
    }

    public MaterialResponse activateMaterial(Long id) {
        return updateMaterial(id, new UpdateMaterialRequest(null, true));
    }

    public MaterialResponse deactivateMaterial(Long id) {
        return updateMaterial(id, new UpdateMaterialRequest(null, false));
    }

    public void deleteMaterial(Long id) {
        Material entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("原料品种不存在: " + id));
        entity.deactivate();
        repository.save(entity);

        eventPublisher.publish(new MaterialChanged(entity.getId(), "DEACTIVATE"));
        log.info("[V2 物料品种] 删除(停用) - ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> listMaterials(Boolean activeOnly) {
        List<Material> materials = Boolean.TRUE.equals(activeOnly)
                ? repository.findByIsActiveTrue()
                : repository.findAll();
        return materials.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MaterialResponse getMaterial(Long id) {
        Material entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("原料品种不存在: " + id));
        return toResponse(entity);
    }

    private MaterialResponse toResponse(Material entity) {
        return new MaterialResponse(entity.getId(), entity.getName(), entity.isActive(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

}
