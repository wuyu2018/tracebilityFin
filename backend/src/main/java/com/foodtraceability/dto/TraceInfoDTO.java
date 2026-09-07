package com.foodtraceability.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraceInfoDTO {

    private ProductInfo product;
    private BatchInfo batch;
    private List<MaterialInfo> materials;
    private InspectionInfo inspection;
    private StorageInfo storage;
    private TransportSaleInfo transportSale;
    private String status;
    private LocalDateTime firstScanTime;
    private Integer scanCount;
    private Boolean isQueried;
    private String queryTip;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private Long id;
        private String name;
        private String specification;
        private String shelfLife;
        private String imageUrl;
        private String contactPhone;
        private String contactEmail;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchInfo {
        private Long id;
        private String batchNumber;
        private LocalDate productionDate;
        private String shelfLife;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialInfo {
        private String materialName;
        private String batchNumber;
        private String supplierName;
        private String producerName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InspectionInfo {
        private String sampleName;
        private Integer sampleQuantity;
        private String sampleSpecification;
        private String imageUrl;
        private String inspectorName;
        private LocalDateTime inspectionTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StorageInfo {
        private LocalDateTime storageTime;
        private LocalDateTime outboundTime;
        private String warehouseLocation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransportSaleInfo {
        private LocalDateTime transportTime;
        private String transportCompany;
        private String vehicleNumber;
        private String salesRegion;
        private String receiverName;
        private String receiverContact;
        private String recorderName;
    }
}