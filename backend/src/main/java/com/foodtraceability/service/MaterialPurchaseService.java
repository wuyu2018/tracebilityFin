package com.foodtraceability.service;

import com.foodtraceability.entity.MaterialPurchase;

import java.util.List;

public interface MaterialPurchaseService {
    List<MaterialPurchase> listAllMaterialPurchases();
    List<MaterialPurchase> getMaterialPurchasesByMaterialId(Long materialId);
}
