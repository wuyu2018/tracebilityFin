package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.foodtraceability.traceability.domain.event.DomainEvent;
import com.foodtraceability.traceability.domain.event.InspectionCompleted;
import com.foodtraceability.traceability.domain.vo.InspectionResult;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inspection")
@Data
@NoArgsConstructor
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Deprecated
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductionBatch batch;

    @Column(name = "sample_name", length = 100)
    private String sampleName;

    @Column(name = "sample_quantity")
    private Integer sampleQuantity;

    @Column(name = "sample_specification", length = 100)
    private String sampleSpecification;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "result_status", length = 20)
    private String resultStatus;

    @Column(name = "result_detail", length = 500)
    private String resultDetail;

    @Column(name = "inspector_name", length = 50)
    private String inspectorName;

    @Column(name = "inspection_time")
    private LocalDateTime inspectionTime;

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public static Inspection create(Long batchId, String sampleName, Integer sampleQuantity, String sampleSpecification) {
        Inspection i = new Inspection();
        i.batchId = batchId;
        i.sampleName = sampleName;
        i.sampleQuantity = sampleQuantity;
        i.sampleSpecification = sampleSpecification;
        return i;
    }

    public void complete(InspectionResult result, String inspectorName) {
        this.resultStatus = result.displayStatus();
        this.resultDetail = result.detail();
        this.inspectorName = inspectorName;
        this.inspectionTime = LocalDateTime.now();
        domainEvents.add(new InspectionCompleted(id, batchId, result));
    }

    public void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> pullEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public boolean isCompleted() {
        return resultStatus != null;
    }

    @Deprecated
    public void setBatch(ProductionBatch batch) {
        this.batchId = batch.getId();
    }
}
