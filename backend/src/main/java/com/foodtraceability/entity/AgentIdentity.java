package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_identity", indexes = {
    @Index(name = "idx_agent_type", columnList = "agent_type"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class AgentIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", unique = true, nullable = false, length = 50)
    private String agentId;

    @Column(name = "agent_type", nullable = false, length = 30)
    private String agentType;

    @Column(name = "certificate_serial", length = 100)
    private String certificateSerial;

    @Column(name = "public_key", columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "credit_score", nullable = false)
    private Long creditScore = 100L;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @PrePersist
    protected void onCreate() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
        if (creditScore == null) {
            creditScore = 100L;
        }
        if (status == null) {
            status = "ACTIVE";
        }
    }
}
