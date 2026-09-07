package com.foodtraceability.validator;

import com.foodtraceability.entity.*;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.MaterialPurchaseRepository;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.repository.ProductionBatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BatchMaterialValidator {
    private static final Logger log = LoggerFactory.getLogger(BatchMaterialValidator.class);

    private final ProductionBatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final MaterialPurchaseRepository materialPurchaseRepository;

    public BatchMaterialValidator(ProductionBatchRepository batchRepository,
                                  ProductRepository productRepository,
                                  MaterialPurchaseRepository materialPurchaseRepository) {
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
        this.materialPurchaseRepository = materialPurchaseRepository;
    }

    public void validate(Long batchId, Long materialPurchaseId) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BusinessException("生产批次不存在: " + batchId));

        MaterialPurchase purchase = materialPurchaseRepository.findById(materialPurchaseId)
                .orElseThrow(() -> new BusinessException("原料采购记录不存在: " + materialPurchaseId));

        Product product = productRepository.findById(batch.getProductId())
                .orElseThrow(() -> new BusinessException("产品不存在: " + batch.getProductId()));
        Material material = purchase.getMaterial();

        if (material == null) {
            throw new BusinessException("原料采购记录未关联原料品种");
        }

        log.debug("[原料校验] 通过 - 产品:{} 原料品种:{}",
                product.getName(), material.getName());
    }
}
