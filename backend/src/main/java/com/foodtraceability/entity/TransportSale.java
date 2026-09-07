package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "transport_sale")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TransportSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductionBatch batch;

    @Column(name = "environment_temperature")
    private Double environmentTemperature;

    @Column(name = "product_temperature")
    private Double productTemperature;

    @Column
    private LocalDateTime time;

    @Column(name = "transport_company", length = 100)
    private String transportCompany;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "sales_region", length = 255)
    private String salesRegion;

    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    @Column(name = "receiver_contact", length = 50)
    private String receiverContact;

    @Column(name = "recorder_name", length = 50)
    private String recorderName;

    public Long getBatchId() {
        return batch != null ? batch.getId() : null;
    }

    public void associateBatch(ProductionBatch batch) {
        this.batch = batch;
    }

    public void record(String recorderName) {
        this.time = LocalDateTime.now();
        this.recorderName = recorderName;
    }
}