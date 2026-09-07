package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "traceability_link", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"batch_id", "entity_type", "entity_id"})
}, indexes = {
    @Index(name = "idx_tl_batch_id", columnList = "batch_id"),
    @Index(name = "idx_tl_entity", columnList = "entity_type, entity_id")
})
@Getter
@Setter
@NoArgsConstructor
public class TraceabilityLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "entity_type", nullable = false, length = 30)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static TraceabilityLink create(Long batchId, String entityType, Long entityId) {
        TraceabilityLink link = new TraceabilityLink();
        link.batchId = batchId;
        link.entityType = entityType;
        link.entityId = entityId;
        return link;
    }
}
