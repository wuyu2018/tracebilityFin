package com.foodtraceability.service;

import com.foodtraceability.dto.SecurityCodeDTO;
import com.foodtraceability.dto.SecurityCodeGenerateResponse;

import java.util.List;

public interface SecurityCodeService {
    SecurityCodeGenerateResponse generateCodes(Long batchId, Integer quantity);
    List<SecurityCodeDTO> getCodesByBatchId(Long batchId);
    SecurityCodeDTO getCodeByCode(String code);
    List<SecurityCodeDTO> exportCodes(Long batchId);
}