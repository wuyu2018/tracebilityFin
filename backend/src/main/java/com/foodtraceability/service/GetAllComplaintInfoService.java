package com.foodtraceability.service;

import com.foodtraceability.dto.ComplaintInfoDTO;

import java.util.List;

public interface GetAllComplaintInfoService {
    List<ComplaintInfoDTO.ComplaintInfo> getAllComplaintInfo();
    boolean deleteComplaintInfo(Long id);
}
