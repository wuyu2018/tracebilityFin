package com.foodtraceability.traceability.domain.service;

import com.foodtraceability.entity.Product;
import com.foodtraceability.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BatchCreationValidatorTest {

    private BatchCreationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BatchCreationValidator();
    }

    @Test
    void validateProduct_valid_succeeds() {
        Product product = new Product();
        product.setId(1L);
        product.setName("测试产品");
        product.setIsDeleted(false);

        Product result = validator.validateProduct(product);
        assertSame(product, result);
    }

    @Test
    void validateProduct_null_throws() {
        assertThrows(BusinessException.class, () -> validator.validateProduct(null));
    }

    @Test
    void validateProduct_deleted_throws() {
        Product product = new Product();
        product.setId(1L);
        product.setIsDeleted(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validateProduct(product));
        assertTrue(ex.getMessage().contains("已被删除"));
    }

    @Test
    void validateMaterialsNotEmpty_withIds_succeeds() {
        assertDoesNotThrow(() -> validator.validateMaterialsNotEmpty(List.of(1L, 2L)));
    }

    @Test
    void validateMaterialsNotEmpty_null_throws() {
        assertThrows(BusinessException.class, () -> validator.validateMaterialsNotEmpty(null));
    }

    @Test
    void validateMaterialsNotEmpty_empty_throws() {
        assertThrows(BusinessException.class, () -> validator.validateMaterialsNotEmpty(List.of()));
    }

    @Test
    void validateShelfLife_valid_succeeds() {
        assertDoesNotThrow(() -> validator.validateShelfLife("12个月"));
    }

    @Test
    void validateShelfLife_null_throws() {
        assertThrows(BusinessException.class, () -> validator.validateShelfLife(null));
    }

    @Test
    void validateShelfLife_blank_throws() {
        assertThrows(BusinessException.class, () -> validator.validateShelfLife(" "));
    }
}
