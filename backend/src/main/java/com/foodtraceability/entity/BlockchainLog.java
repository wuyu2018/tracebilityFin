package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "blockchain_log", indexes = {
    @Index(name = "idx_bl_batch_id", columnList = "batch_id"),
    @Index(name = "idx_bl_timestamp", columnList = "timestamp"),
    @Index(name = "idx_bl_chain_type", columnList = "chain_type"),
    @Index(name = "idx_bl_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_bl_data_hash", columnList = "data_hash")
})
@Getter
@Setter
@NoArgsConstructor
public class BlockchainLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_header_id")
    private BlockHeader blockHeader;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "chain_type", nullable = false, length = 30)
    private String chainType;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Column(name = "previous_hash", length = 128)
    private String previousHash;

    @Column(name = "current_hash", nullable = false, length = 128)
    private String currentHash;

    @Deprecated
    @Column(name = "data_snapshot", columnDefinition = "TEXT")
    private String dataSnapshot;

    @Column(name = "data_hash", length = 64)
    private String dataHash;

    @Column(name = "offchain_ref", length = 200)
    private String offchainReference;

    @Column(name = "signature", nullable = false, length = 512)
    private String signature;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "ref_master_chain_hash", length = 128)
    private String refMasterChainHash;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public static BlockchainLog createOptimizedBlock(String chainType, Long batchId, String entityType, Long entityId,
                                                       String action, String previousHash, String currentHash,
                                                       String signature, LocalDateTime timestamp, Long operatorId,
                                                       String dataHash, String offchainRef, String refMasterChainHash) {
        BlockchainLog block = new BlockchainLog();
        block.chainType = chainType;
        block.batchId = batchId;
        block.entityType = entityType;
        block.entityId = entityId;
        block.action = action;
        block.previousHash = previousHash;
        block.currentHash = currentHash;
        block.signature = signature;
        block.timestamp = timestamp;
        block.operatorId = operatorId;
        block.dataHash = dataHash;
        block.offchainReference = offchainRef;
        block.refMasterChainHash = refMasterChainHash;
        return block;
    }
}
