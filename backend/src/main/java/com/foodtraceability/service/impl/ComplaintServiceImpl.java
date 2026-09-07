package com.foodtraceability.service.impl;

import com.foodtraceability.dto.ComplaintDTO;
import com.foodtraceability.entity.Complaint;
import com.foodtraceability.entity.SecurityCode;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.ComplaintRepository;
import com.foodtraceability.repository.SecurityCodeRepository;
import com.foodtraceability.service.ComplaintService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private static final Logger log = LoggerFactory.getLogger(ComplaintServiceImpl.class);

    private final ComplaintRepository complaintRepository;
    private final SecurityCodeRepository securityCodeRepository;

    public ComplaintServiceImpl(ComplaintRepository complaintRepository,
                               SecurityCodeRepository securityCodeRepository) {
        this.complaintRepository = complaintRepository;
        this.securityCodeRepository = securityCodeRepository;
    }

    @Override
    @Transactional
    public ComplaintDTO createComplaint(ComplaintDTO complaintDTO) {
        if (complaintDTO == null) {
            throw new BusinessException("投诉信息不能为空");
        }

        SecurityCode securityCode = securityCodeRepository.findByCode(complaintDTO.getAntiFakeCode())
                .orElseThrow(() -> new BusinessException("防伪码不存在: " + complaintDTO.getAntiFakeCode()));

        Complaint complaint = Complaint.create(securityCode, complaintDTO.getComplaintReason());

        Complaint savedComplaint = complaintRepository.save(complaint);
        log.info("[投诉创建] 投诉已保存 - ID: {}, 防伪码: {}",
                savedComplaint.getId(), complaintDTO.getAntiFakeCode());

        return toDTO(savedComplaint);
    }

    @Override
    @Transactional
    public ComplaintDTO updateComplaintReason(Long id, String complaintReason) {
        if (id == null || id <= 0) {
            throw new BusinessException("投诉ID不能为空或无效");
        }

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new BusinessException("投诉记录不存在"));

        complaint.updateReason(complaintReason);
        Complaint updatedComplaint = complaintRepository.save(complaint);

        log.info("[投诉更新] 投诉原因已更新 - ID: {}", updatedComplaint.getId());

        return toDTO(updatedComplaint);
    }

    private ComplaintDTO toDTO(Complaint complaint) {
        ComplaintDTO dto = new ComplaintDTO();
        dto.setId(complaint.getId());
        dto.setComplaintReason(complaint.getComplaintReason());
        dto.setComplaintTime(complaint.getComplaintTime());
        dto.setAntiFakeCode(complaint.getAntiFakeCode());
        dto.setBatchNumber(complaint.getBatchNumber());
        dto.setProductName(complaint.getProductName());
        return dto;
    }
}
