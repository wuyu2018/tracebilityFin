package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "blockchain_retry_task", indexes = {
    @Index(name = "idx_brt_status", columnList = "status"),
    @Index(name = "idx_brt_next_retry", columnList = "next_retry_at")
})
@Getter
@Setter
@NoArgsConstructor
public class BlockchainRetryTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chain_type", nullable = false, length = 30)
    private String chainType;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(name = "raw_data", nullable = false, columnDefinition = "TEXT")
    private String rawData;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private RetryStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "next_retry_at", nullable = false)
    private LocalDateTime nextRetryAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum RetryStatus {
        PENDING, PROCESSING, SUCCESS, FAILED
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (retryCount == null) {
            retryCount = 0;
        }
        if (maxRetries == null) {
            maxRetries = 5;
        }
        if (status == null) {
            status = RetryStatus.PENDING;
        }
        if (nextRetryAt == null) {
            nextRetryAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void incrementRetry() {
        this.retryCount++;
        long delayMinutes = (long) Math.pow(2, retryCount - 1);
        this.nextRetryAt = LocalDateTime.now().plusMinutes(delayMinutes);
    }
}
