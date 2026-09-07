package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "consensus_state", indexes = {
    @Index(name = "idx_cs_seq_num", columnList = "sequence_number"),
    @Index(name = "idx_cs_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class ConsensusRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sequence_number", nullable = false)
    private Long sequenceNumber;

    @Column(nullable = false, length = 10)
    private String view;

    @Column(length = 128)
    private String digest;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ConsensusPhase phase;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ConsensusStatus status;

    @Column(name = "prepare_count")
    private Integer prepareCount;

    @Column(name = "commit_count")
    private Integer commitCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum ConsensusPhase {
        ENDORSEMENT, PRE_PREPARE, PREPARE, COMMIT, EXECUTED
    }

    public enum ConsensusStatus {
        PENDING, ACCEPTED, REJECTED, COMMITTED
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (prepareCount == null) {
            prepareCount = 0;
        }
        if (commitCount == null) {
            commitCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
