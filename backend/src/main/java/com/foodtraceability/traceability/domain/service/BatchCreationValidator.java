package com.foodtraceability.traceability.domain.service;

import com.foodtraceability.entity.Product;
import com.foodtraceability.exception.BusinessException;

public class BatchCreationValidator {

    public Product validateProduct(Product product) {
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        if (product.isDeleted()) {
            throw new BusinessException("产品已被删除，无法创建批次");
        }
        return product;
    }

    public void validateMaterialsNotEmpty(java.util.List<Long> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) {
            throw new BusinessException("批次必须关联至少一种原料采购记录");
        }
    }

    public void validateShelfLife(String shelfLife) {
        if (shelfLife == null || shelfLife.isBlank()) {
            throw new BusinessException("保质期不能为空");
        }
    }
}
