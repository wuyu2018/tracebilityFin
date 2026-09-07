package com.foodtraceability.service.impl;

import com.foodtraceability.dto.ComplaintInfoDTO;
import com.foodtraceability.repository.ComplaintRepository;
import com.foodtraceability.entity.Complaint;
import com.foodtraceability.service.GetAllComplaintInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GetAllComplaintInfoServiceImpl implements GetAllComplaintInfoService {

    private final ComplaintRepository complaintRepository;

    public GetAllComplaintInfoServiceImpl(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    @Override
    public List<ComplaintInfoDTO.ComplaintInfo> getAllComplaintInfo() {
        List<Complaint> complaints = complaintRepository.findAll();
        return complaints.stream()
                .map(this::convertToComplaintInfoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteComplaintInfo(Long id) {
        try {
            if (complaintRepository.existsById(id)) {
                complaintRepository.deleteById(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("删除投诉失败: " + e.getMessage());
        }
    }

    private ComplaintInfoDTO.ComplaintInfo convertToComplaintInfoDTO(Complaint complaint) {
        ComplaintInfoDTO.ComplaintInfo info = new ComplaintInfoDTO.ComplaintInfo();
        info.setId(complaint.getId());
        info.setComplaintReason(complaint.getComplaintReason());
        info.setComplaintTime(complaint.getComplaintTime());
        info.setBatchNumber(complaint.getBatchNumber());
        info.setProductName(complaint.getProductName());
        return info;
    }
}
