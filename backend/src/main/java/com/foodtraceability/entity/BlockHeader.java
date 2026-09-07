package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "block_header", indexes = {
    @Index(name = "idx_bh_chain_type", columnList = "chain_type"),
    @Index(name = "idx_bh_timestamp", columnList = "timestamp"),
    @Index(name = "idx_bh_block_hash", columnList = "block_hash")
})
@Getter
@Setter
@NoArgsConstructor
public class BlockHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chain_type", nullable = false, length = 30)
    private String chainType;

    @Column(name = "block_hash", nullable = false, length = 128)
    private String blockHash;

    @Column(name = "previous_hash", length = 128)
    private String previousHash;

    @Column(name = "merkle_root", length = 128)
    private String merkleRoot;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "bloom_filter", columnDefinition = "MEDIUMBLOB")
    private byte[] bloomFilter;

    @Column(name = "metadata_index", columnDefinition = "JSON")
    private String metadataIndex;

    @Column(name = "tx_count")
    private Integer txCount;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public static BlockHeader create(String chainType, String previousHash, String merkleRoot,
                                      byte[] bloomFilter, String metadataIndex, Integer txCount,
                                      LocalDateTime timestamp) {
        BlockHeader header = new BlockHeader();
        header.chainType = chainType;
        header.previousHash = previousHash;
        header.merkleRoot = merkleRoot;
        header.bloomFilter = bloomFilter;
        header.metadataIndex = metadataIndex;
        header.txCount = txCount;
        header.timestamp = timestamp;
        String raw = chainType + "|" + (previousHash != null ? previousHash : "") + "|"
                     + merkleRoot + "|" + timestamp.truncatedTo(java.time.temporal.ChronoUnit.MILLIS) + "|" + txCount;
        header.blockHash = sha256Hex(raw);
        return header;
    }

    private static String sha256Hex(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
