package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "smart_contract_state", uniqueConstraints = {
    @UniqueConstraint(name = "uk_contract_state", columnNames = {"contract_id", "state_key"})
})
@Getter
@Setter
@NoArgsConstructor
public class SmartContractState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id", nullable = false, length = 50)
    private String contractId;

    @Column(name = "contract_type", nullable = false, length = 50)
    private String contractType;

    @Column(name = "state_key", nullable = false, length = 200)
    private String stateKey;

    @Column(name = "state_value", columnDefinition = "TEXT")
    private String stateValue;

    @Column(nullable = false)
    private Long version;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (version == null) {
            version = 1L;
        }
    }
}
