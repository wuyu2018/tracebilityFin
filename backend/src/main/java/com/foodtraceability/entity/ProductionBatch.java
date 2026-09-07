package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.foodtraceability.traceability.domain.event.DomainEvent;
import com.foodtraceability.traceability.domain.vo.BatchNumber;
import com.foodtraceability.traceability.domain.vo.Quantity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_batch", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "batch_number"})
})
@Getter
@Setter
@NoArgsConstructor
public class ProductionBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Deprecated
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;

    @Column(name = "shelf_life", length = 50)
    private String shelfLife;

    @Column(name = "quantity")
    private Double quantity;

    @Column(length = 20)
    private String unit;

    @Column(name = "storage_id")
    private Long storageId;

    @Column(name = "transport_sale_id")
    private Long transportSaleId;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static ProductionBatch create(BatchNumber batchNo, Long productId, LocalDate productionDate,
                                          String shelfLife, Quantity qty) {
        ProductionBatch batch = new ProductionBatch();
        batch.batchNumber = batchNo.value();
        batch.productId = productId;
        batch.productionDate = productionDate;
        batch.shelfLife = shelfLife;
        batch.quantity = qty.value();
        batch.unit = qty.unit();
        batch.isDeleted = false;
        return batch;
    }

    public void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> pullEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.isDeleted);
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    public void associateStorage(Storage storage) {
        this.storageId = storage.getId();
    }

    public void associateTransportSale(TransportSale transportSale) {
        this.transportSaleId = transportSale.getId();
    }

    public void changeProductionDate(LocalDate productionDate) {
        if (productionDate != null) this.productionDate = productionDate;
    }

    public void changeShelfLife(String shelfLife) {
        if (shelfLife != null) this.shelfLife = shelfLife;
    }

    public void changeQuantity(Double quantity) {
        if (quantity != null) this.quantity = quantity;
    }

    public void changeUnit(String unit) {
        if (unit != null) this.unit = unit;
    }

    public static ProductionBatch quickCreate(String batchNumber, Long productId, String shelfLife) {
        ProductionBatch batch = new ProductionBatch();
        batch.batchNumber = batchNumber;
        batch.productId = productId;
        batch.productionDate = LocalDate.now();
        batch.shelfLife = shelfLife;
        batch.quantity = 0.0;
        batch.unit = "";
        batch.isDeleted = false;
        return batch;
    }

    public void markQualified() {
    }

    public void markUnqualified() {
    }

    public void associateInspection(Long inspectionId) {
    }
}
