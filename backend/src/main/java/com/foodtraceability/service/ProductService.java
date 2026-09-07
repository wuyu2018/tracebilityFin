package com.foodtraceability.service;

import com.foodtraceability.dto.ProductDTO;
import com.foodtraceability.entity.Product;

import java.util.List;

public interface ProductService {
    Product createProduct(ProductDTO dto);
    Product updateProduct(Long id, ProductDTO dto);
    void deleteProduct(Long id);
    void hardDeleteProduct(Long id);
    List<Product> listAllProducts();
    List<Product> searchProducts(String keyword);
    Product getProductById(Long id);
}
