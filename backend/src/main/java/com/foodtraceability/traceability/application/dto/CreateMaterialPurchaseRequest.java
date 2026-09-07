package com.foodtraceability.traceability.application.dto;

import java.time.LocalDateTime;

public record CreateMaterialPurchaseRequest(Long materialId, String batchNumber,
                                            String supplierName, String producerName,
                                            String producerAddress, LocalDateTime purchaseDate,
                                            Double quantity, String unit) {}
