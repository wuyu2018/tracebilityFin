package com.foodtraceability.validator;

import com.foodtraceability.entity.*;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.MaterialPurchaseRepository;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.repository.ProductionBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchMaterialValidatorTest {

    @Mock
    private ProductionBatchRepository batchRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private MaterialPurchaseRepository materialPurchaseRepository;

    private BatchMaterialValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BatchMaterialValidator(
                batchRepository, productRepository, materialPurchaseRepository);
    }

    @Test
    void validate_passes_whenEntitiesExist() {
        Product product = createProduct(1L, "有机纯牛奶");
        Material material = createMaterial(10L, "有机生牛乳");
        MaterialPurchase purchase = createMaterialPurchase(100L, material);
        ProductionBatch batch = createBatch(50L, product);

        when(batchRepository.findById(50L)).thenReturn(Optional.of(batch));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(materialPurchaseRepository.findById(100L)).thenReturn(Optional.of(purchase));

        assertDoesNotThrow(() -> validator.validate(50L, 100L));
    }

    @Test
    void validate_throws_whenBatchNotFound() {
        when(batchRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate(999L, 100L));
        assertTrue(ex.getMessage().contains("生产批次不存在"));
    }

    @Test
    void validate_throws_whenPurchaseNotFound() {
        ProductionBatch batch = createBatch(50L, createProduct(1L, "有机纯牛奶"));
        when(batchRepository.findById(50L)).thenReturn(Optional.of(batch));
        when(materialPurchaseRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate(50L, 999L));
        assertTrue(ex.getMessage().contains("原料采购记录不存在"));
    }

    @Test
    void validate_throws_whenMaterialIsNull() {
        Product product = createProduct(1L, "有机纯牛奶");
        MaterialPurchase purchase = createMaterialPurchase(100L, null);
        ProductionBatch batch = createBatch(50L, product);

        when(batchRepository.findById(50L)).thenReturn(Optional.of(batch));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(materialPurchaseRepository.findById(100L)).thenReturn(Optional.of(purchase));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate(50L, 100L));
        assertTrue(ex.getMessage().contains("未关联原料品种"));
    }

    @Test
    void validate_throws_whenProductNotFound() {
        Product product = createProduct(1L, "有机纯牛奶");
        Material material = createMaterial(10L, "有机生牛乳");
        MaterialPurchase purchase = createMaterialPurchase(100L, material);
        ProductionBatch batch = createBatch(50L, product);

        when(batchRepository.findById(50L)).thenReturn(Optional.of(batch));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        when(materialPurchaseRepository.findById(100L)).thenReturn(Optional.of(purchase));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate(50L, 100L));
        assertTrue(ex.getMessage().contains("产品不存在"));
    }

    private Product createProduct(Long id, String name) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        return p;
    }

    private Material createMaterial(Long id, String name) {
        Material m = new Material();
        m.setId(id);
        m.setName(name);
        return m;
    }

    private MaterialPurchase createMaterialPurchase(Long id, Material material) {
        MaterialPurchase mp = new MaterialPurchase();
        mp.setId(id);
        mp.setMaterial(material);
        return mp;
    }

    private ProductionBatch createBatch(Long id, Product product) {
        ProductionBatch b = new ProductionBatch();
        b.setId(id);
        b.setProductId(product.getId());
        return b;
    }
}
