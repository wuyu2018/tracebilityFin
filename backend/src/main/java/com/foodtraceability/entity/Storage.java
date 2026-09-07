package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.foodtraceability.traceability.domain.event.DomainEvent;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "storage")
@Getter
@Setter
@NoArgsConstructor
public class Storage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Deprecated
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductionBatch batch;

    @Column(name = "storage_time")
    private LocalDateTime storageTime;

    @Column(name = "outbound_time")
    private LocalDateTime outboundTime;

    @Column
    private Double quantity;

    @Column(length = 20)
    private String unit;

    @Column(name = "warehouse_location", length = 100)
    private String warehouseLocation;

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public static Storage create(Long batchId, LocalDateTime storageTime, LocalDateTime outboundTime,
                                  Double quantity, String unit, String warehouseLocation) {
        Storage s = new Storage();
        s.batchId = batchId;
        s.storageTime = storageTime;
        s.outboundTime = outboundTime;
        s.quantity = quantity;
        s.unit = unit;
        s.warehouseLocation = warehouseLocation;
        return s;
    }

    public void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> pullEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    @Deprecated
    public void associateBatch(ProductionBatch batch) {
        this.batchId = batch.getId();
    }
}
