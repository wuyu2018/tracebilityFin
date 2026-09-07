package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "offchain_storage", indexes = {
    @Index(name = "idx_offchain_food_id", columnList = "food_id"),
    @Index(name = "idx_offchain_storage", columnList = "storage_type, storage_key"),
    @Index(name = "idx_offchain_owner", columnList = "owner_agent_id")
})
@Getter
@Setter
public class OffchainStorage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "food_id", unique = true, length = 100, nullable = false)
    private String foodId;
    
    @Column(name = "data_hash", nullable = false, length = 64)
    private String dataHash;
    
    @Column(name = "storage_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StorageType storageType;
    
    @Column(name = "storage_key", nullable = false, length = 200)
    private String storageKey;
    
    @Column(name = "encryption_method", length = 50)
    private String encryptionMethod;
    
    @Column(name = "encrypted_data", columnDefinition = "TEXT")
    private String encryptedData;
    
    @Column(name = "encrypted_aes_key", columnDefinition = "TEXT")
    private String encryptedAesKey;
    
    @Column(name = "owner_agent_id", nullable = false)
    private Long ownerAgentId;
    
    @Column(name = "access_policy", columnDefinition = "JSON")
    private String accessPolicy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
    }
    
    public enum StorageType {
        REDIS,
        LOCAL_FILE,
        OSS,
        DATABASE
    }
}
