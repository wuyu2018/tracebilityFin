package com.foodtraceability.service;

import com.foodtraceability.dto.ProductionBatchDTO;
import com.foodtraceability.entity.ProductionBatch;

import java.util.List;

public interface ProductionBatchService {
    ProductionBatch updateBatch(Long id, ProductionBatchDTO dto);
    void deleteBatch(Long id);
    List<ProductionBatchDTO> listAllBatches();
    List<ProductionBatchDTO> getBatchesByProductId(Long productId);
    ProductionBatchDTO getBatchById(Long id);
    ProductionBatch getBatchByBatchNumber(String batchNumber);

    ProductionBatch createQuickBatchForProduct(Long productId);
}