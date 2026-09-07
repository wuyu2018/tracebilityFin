package com.foodtraceability.service.impl;

import com.foodtraceability.entity.MaterialPurchase;
import com.foodtraceability.repository.MaterialPurchaseRepository;
import com.foodtraceability.service.MaterialPurchaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
public class MaterialPurchaseServiceImpl implements MaterialPurchaseService {
    private static final Logger log = LoggerFactory.getLogger(MaterialPurchaseServiceImpl.class);

    private final MaterialPurchaseRepository repository;

    public MaterialPurchaseServiceImpl(MaterialPurchaseRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialPurchase> listAllMaterialPurchases() {
        return repository.findByIsDeletedFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialPurchase> getMaterialPurchasesByMaterialId(Long materialId) {
        return repository.findByMaterialIdAndIsDeletedFalse(materialId);
    }
}
