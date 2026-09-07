package com.foodtraceability.anchor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blockchain_anchor", indexes = {
    @Index(name = "idx_ba_batch_date", columnList = "batch_id, anchor_date")
})
@Getter
@Setter
@NoArgsConstructor
public class BlockchainAnchor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chain_type", nullable = false, length = 30)
    private String chainType;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "current_hash", nullable = false, length = 128)
    private String currentHash;

    @Column(name = "anchor_date", nullable = false)
    private LocalDate anchorDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static BlockchainAnchor create(String chainType, Long batchId, String currentHash, LocalDate anchorDate) {
        BlockchainAnchor a = new BlockchainAnchor();
        a.chainType = chainType;
        a.batchId = batchId;
        a.currentHash = currentHash;
        a.anchorDate = anchorDate;
        return a;
    }
}
