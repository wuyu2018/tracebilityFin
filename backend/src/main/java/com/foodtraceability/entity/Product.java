package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String specification;

    @Column(name = "shelf_life", length = 50)
    private String shelfLife;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.isDeleted);
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    public void changeName(String name) {
        if (name != null) this.name = name;
    }

    public void changeSpecification(String specification) {
        if (specification != null) this.specification = specification;
    }

    public void changeShelfLife(String shelfLife) {
        if (shelfLife != null) this.shelfLife = shelfLife;
    }

    public void changeImageUrl(String imageUrl) {
        if (imageUrl != null) this.imageUrl = imageUrl;
    }

    public void changeContactPhone(String contactPhone) {
        if (contactPhone != null) this.contactPhone = contactPhone;
    }

    public void changeContactEmail(String contactEmail) {
        if (contactEmail != null) this.contactEmail = contactEmail;
    }
}