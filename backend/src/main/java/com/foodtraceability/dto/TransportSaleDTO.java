package com.foodtraceability.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TransportSaleDTO {
    private Long id;
    private Long batchId;
    private Double environmentTemperature;
    private Double productTemperature;
    private LocalDateTime time;
    private String transportCompany;
    private String vehicleNumber;
    private String salesRegion;
    private String receiverName;
    private String receiverContact;
}