package com.foodtraceability.service.impl;

import com.foodtraceability.entity.Material;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.MaterialRepository;
import com.foodtraceability.service.MaterialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaterialServiceImpl implements MaterialService {
    private static final Logger log = LoggerFactory.getLogger(MaterialServiceImpl.class);

    private final MaterialRepository repository;

    public MaterialServiceImpl(MaterialRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Material getMaterialById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("原料品种不存在: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Material> listAllActiveMaterials() {
        return repository.findByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Material> listAllMaterials() {
        return repository.findAll();
    }
}
