package com.foodtraceability.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String specification;
    private String shelfLife;
    private String imageUrl;
    private String contactPhone;
    private String contactEmail;
}