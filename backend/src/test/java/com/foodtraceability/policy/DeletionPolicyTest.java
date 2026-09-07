package com.foodtraceability.policy;

import com.foodtraceability.entity.Product;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeletionPolicyTest {

    @Mock
    private ProductionBatchRepository batchRepository;
    @Mock
    private SecurityCodeRepository securityCodeRepository;
    @Mock
    private ProductRepository productRepository;

    private DeletionPolicy deletionPolicy;

    @BeforeEach
    void setUp() {
        deletionPolicy = new DeletionPolicy(
                batchRepository, securityCodeRepository, productRepository);
    }

    @Test
    void softDeleteProduct_whenHasBatches() {
        Product product = createProduct(1L, "有机纯牛奶");
        when(batchRepository.existsByProductId(1L)).thenReturn(true);

        deletionPolicy.deleteProduct(product);

        verify(productRepository).save(product);
        assertTrue(product.isDeleted());
        verify(productRepository, never()).delete(any());
    }

    @Test
    void softDeleteProduct_whenHasSecurityCodes() {
        Product product = createProduct(2L, "有机酸奶");
        when(batchRepository.existsByProductId(2L)).thenReturn(false);
        when(securityCodeRepository.existsByBatch_Product_Id(2L)).thenReturn(true);

        deletionPolicy.deleteProduct(product);

        verify(productRepository).save(product);
        assertTrue(product.isDeleted());
        verify(productRepository, never()).delete(any());
    }

    @Test
    void physicallyDeleteProduct_whenNoBatchesAndNoCodes() {
        Product product = createProduct(3L, "测试产品");
        when(batchRepository.existsByProductId(3L)).thenReturn(false);
        when(securityCodeRepository.existsByBatch_Product_Id(3L)).thenReturn(false);

        deletionPolicy.deleteProduct(product);

        verify(productRepository).delete(product);
        verify(productRepository, never()).save(any());
    }

    @Test
    void hardDeleteProduct_succeeds_whenNoAssociations() {
        Product product = createProduct(4L, "测试产品");
        when(batchRepository.existsByProductId(4L)).thenReturn(false);
        when(securityCodeRepository.existsByBatch_Product_Id(4L)).thenReturn(false);

        deletionPolicy.hardDeleteProduct(product);

        verify(productRepository).delete(product);
    }

    @Test
    void hardDeleteProduct_throws_whenHasBatches() {
        Product product = createProduct(5L, "有批次产品");
        when(batchRepository.existsByProductId(5L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> deletionPolicy.hardDeleteProduct(product));
        assertTrue(ex.getMessage().contains("禁止物理删除"));
        verify(productRepository, never()).delete(any());
    }

    @Test
    void hardDeleteProduct_throws_whenHasCodes() {
        Product product = createProduct(6L, "有码产品");
        when(batchRepository.existsByProductId(6L)).thenReturn(false);
        when(securityCodeRepository.existsByBatch_Product_Id(6L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> deletionPolicy.hardDeleteProduct(product));
        assertTrue(ex.getMessage().contains("禁止物理删除"));
        verify(productRepository, never()).delete(any());
    }

    private Product createProduct(Long id, String name) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setIsDeleted(false);
        return p;
    }
}
