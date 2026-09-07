package com.foodtraceability.service;

import com.foodtraceability.entity.Material;

import java.util.List;

public interface MaterialService {
    Material getMaterialById(Long id);

    List<Material> listAllActiveMaterials();

    List<Material> listAllMaterials();
}
