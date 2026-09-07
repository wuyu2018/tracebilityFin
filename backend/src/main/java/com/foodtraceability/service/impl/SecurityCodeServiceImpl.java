package com.foodtraceability.service.impl;

import com.foodtraceability.dto.SecurityCodeDTO;
import com.foodtraceability.dto.SecurityCodeGenerateResponse;
import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.entity.SecurityCode;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.repository.SecurityCodeRepository;
import com.foodtraceability.service.SecurityCodeService;
import com.foodtraceability.traceability.domain.event.SecurityCodesGenerated;
import com.foodtraceability.traceability.infrastructure.messaging.DomainEventPublisherImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SecurityCodeServiceImpl implements SecurityCodeService {
    private final SecurityCodeRepository codeRepository;
    private final ProductionBatchRepository batchRepository;
    private final DomainEventPublisherImpl eventPublisher;

    public SecurityCodeServiceImpl(SecurityCodeRepository codeRepository,
                                 ProductionBatchRepository batchRepository,
                                 DomainEventPublisherImpl eventPublisher) {
        this.codeRepository = codeRepository;
        this.batchRepository = batchRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public SecurityCodeGenerateResponse generateCodes(Long batchId, Integer quantity) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));

        List<String> codes = generateCodesForBatch(batch, quantity);

        eventPublisher.publish(new SecurityCodesGenerated(batchId, quantity));

        SecurityCodeGenerateResponse response = new SecurityCodeGenerateResponse();
        response.setCodes(codes);
        response.setCount(codes.size());
        return response;
    }

    private List<String> generateCodesForBatch(ProductionBatch batch, Integer quantity) {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            SecurityCode securityCode = SecurityCode.create(batch);
            codeRepository.save(securityCode);
            codes.add(securityCode.getCode());
        }
        return codes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecurityCodeDTO> getCodesByBatchId(Long batchId) {
        return codeRepository.findByBatch_Id(batchId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SecurityCodeDTO getCodeByCode(String code) {
        return codeRepository.findByCode(code)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("防伪码不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecurityCodeDTO> exportCodes(Long batchId) {
        return getCodesByBatchId(batchId);
    }

    private SecurityCodeDTO toDTO(SecurityCode entity) {
        SecurityCodeDTO dto = new SecurityCodeDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setBatchId(entity.getBatch().getId());
        dto.setBatchNumber(entity.getBatch().getBatchNumber());
        dto.setStatus(entity.getStatus());
        dto.setFirstScanTime(entity.getFirstScanTime());
        dto.setScanCount(entity.getScanCount());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}