package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "security_code", indexes = {
    @Index(name = "idx_security_code", columnList = "code", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SecurityCode {

    public static final String STATUS_INACTIVE = "未激活";
    public static final String STATUS_ACTIVE = "已激活";
    public static final String STATUS_FROZEN = "已冻结";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductionBatch batch;

    public Long getBatchId() {
        return batch != null ? batch.getId() : null;
    }

    @Column(name = "status", nullable = false, length = 20)
    private String status = STATUS_INACTIVE;

    @Column(name = "first_scan_time")
    private LocalDateTime firstScanTime;

    @Column(name = "scan_count")
    private Integer scanCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (scanCount == null) {
            scanCount = 0;
        }
        if (status == null) {
            status = STATUS_INACTIVE;
        }
    }

    public static String generateUniqueCode() {
        String snowflake = String.valueOf(System.currentTimeMillis());
        String random = String.format("%04d", (int) (Math.random() * 10000));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "SC" + snowflake + random + uuid;
    }

    public static SecurityCode create(ProductionBatch batch) {
        SecurityCode securityCode = new SecurityCode();
        securityCode.code = generateUniqueCode();
        securityCode.batch = batch;
        securityCode.status = STATUS_INACTIVE;
        securityCode.scanCount = 0;
        return securityCode;
    }

    public boolean isActivated() {
        return STATUS_ACTIVE.equals(this.status);
    }

    public boolean isFirstQuery() {
        return scanCount == null || scanCount == 0;
    }

    public boolean isRepeatedQuery() {
        return scanCount != null && scanCount > 1;
    }

    public int getQueryCount() {
        return scanCount != null ? scanCount : 0;
    }

    public void activate() {
        this.status = STATUS_ACTIVE;
        this.firstScanTime = LocalDateTime.now();
        this.scanCount = 1;
    }

    public void recordQuery() {
        if (this.scanCount == null) {
            this.scanCount = 0;
        }
        this.scanCount++;
    }

    public static String maskCode(String code) {
        if (code == null || code.length() <= 8) return "***";
        return code.substring(0, 4) + "****" + code.substring(code.length() - 4);
    }

    public void freeze() {
        this.status = STATUS_FROZEN;
    }

    public void recordQueryAndActivateIfNeeded() {
        if (isFirstQuery()) {
            activate();
        } else {
            recordQuery();
        }
    }
}