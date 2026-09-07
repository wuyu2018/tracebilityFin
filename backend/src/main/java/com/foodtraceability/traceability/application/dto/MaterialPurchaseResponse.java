package com.foodtraceability.traceability.application.dto;

import java.time.LocalDateTime;

public record MaterialPurchaseResponse(Long id, Long materialId, String materialName,
                                       String batchNumber, String supplierName,
                                       String producerName, String producerAddress,
                                       LocalDateTime purchaseDate, Double quantity,
                                       String unit, boolean isDeleted) {}
