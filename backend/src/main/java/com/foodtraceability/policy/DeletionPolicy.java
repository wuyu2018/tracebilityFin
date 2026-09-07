package com.foodtraceability.policy;

import com.foodtraceability.entity.Product;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeletionPolicy {
    private static final Logger log = LoggerFactory.getLogger(DeletionPolicy.class);

    private final ProductionBatchRepository batchRepository;
    private final SecurityCodeRepository securityCodeRepository;
    private final ProductRepository productRepository;

    public DeletionPolicy(ProductionBatchRepository batchRepository,
                          SecurityCodeRepository securityCodeRepository,
                          ProductRepository productRepository) {
        this.batchRepository = batchRepository;
        this.securityCodeRepository = securityCodeRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void deleteProduct(Product product) {
        Long productId = product.getId();
        log.info("[DeletionPolicy] 删除产品 - ID: {}, 名称: {}", productId, product.getName());

        boolean hasBatches = batchRepository.existsByProductId(productId);
        boolean hasCodes = securityCodeRepository.existsByBatch_Product_Id(productId);

        if (!hasBatches && !hasCodes) {
            physicallyDeleteProduct(product);
        } else {
            softDeleteProduct(product);
        }
    }

    @Transactional
    public void hardDeleteProduct(Product product) {
        Long productId = product.getId();
        boolean hasBatches = batchRepository.existsByProductId(productId);
        boolean hasCodes = securityCodeRepository.existsByBatch_Product_Id(productId);

        if (hasBatches || hasCodes) {
            throw new BusinessException(
                    String.format("产品 ID:%d 有关联的生产批次或防伪码，禁止物理删除", productId));
        }
        physicallyDeleteProduct(product);
    }

    private void physicallyDeleteProduct(Product product) {
        Long productId = product.getId();
        log.info("[DeletionPolicy] 物理删除产品 - ID: {}", productId);

        productRepository.delete(product);

        log.info("[DeletionPolicy] 产品已物理删除 - ID: {}", productId);
    }

    private void softDeleteProduct(Product product) {
        Long productId = product.getId();
        log.info("[DeletionPolicy] 软删除产品 - ID: {}", productId);

        product.softDelete();
        productRepository.save(product);

        log.info("[DeletionPolicy] 产品已软删除 - ID: {}", productId);
    }
}
