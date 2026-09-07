package com.foodtraceability.traceability.interfaces.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TraceabilityQueryResponse {
    private final ProductInfo product;
    private final BatchInfo batch;
    private final List<MaterialInfo> materials;
    private final InspectionInfo inspection;
    private final StorageInfo storage;
    private final TransportSaleInfo transportSale;
    private final String status;
    private final Boolean isRepeatedQuery;
    private final Integer scanCount;
    private final String firstScanTime;
    private final String queryTip;

    public TraceabilityQueryResponse(ProductInfo product, BatchInfo batch, List<MaterialInfo> materials,
                                     InspectionInfo inspection, StorageInfo storage, TransportSaleInfo transportSale,
                                     String status, Boolean isRepeatedQuery, Integer scanCount,
                                     String firstScanTime, String queryTip) {
        this.product = product;
        this.batch = batch;
        this.materials = materials;
        this.inspection = inspection;
        this.storage = storage;
        this.transportSale = transportSale;
        this.status = status;
        this.isRepeatedQuery = isRepeatedQuery;
        this.scanCount = scanCount;
        this.firstScanTime = firstScanTime;
        this.queryTip = queryTip;
    }

    public ProductInfo getProduct() { return product; }
    public BatchInfo getBatch() { return batch; }
    public List<MaterialInfo> getMaterials() { return materials; }
    public InspectionInfo getInspection() { return inspection; }
    public StorageInfo getStorage() { return storage; }
    public TransportSaleInfo getTransportSale() { return transportSale; }
    public String getStatus() { return status; }
    public Boolean getIsRepeatedQuery() { return isRepeatedQuery; }
    public Integer getScanCount() { return scanCount; }
    public String getFirstScanTime() { return firstScanTime; }
    public String getQueryTip() { return queryTip; }

    public static class ProductInfo {
        private final Long id;
        private final String name;
        private final String specification;
        private final String shelfLife;
        private final String imageUrl;
        private final String contactPhone;
        private final String contactEmail;

        public ProductInfo(Long id, String name, String specification, String shelfLife,
                          String imageUrl, String contactPhone, String contactEmail) {
            this.id = id;
            this.name = name;
            this.specification = specification;
            this.shelfLife = shelfLife;
            this.imageUrl = imageUrl;
            this.contactPhone = contactPhone;
            this.contactEmail = contactEmail;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getSpecification() { return specification; }
        public String getShelfLife() { return shelfLife; }
        public String getImageUrl() { return imageUrl; }
        public String getContactPhone() { return contactPhone; }
        public String getContactEmail() { return contactEmail; }
    }

    public static class BatchInfo {
        private final Long id;
        private final String batchNumber;
        private final LocalDate productionDate;
        private final String shelfLife;
        private final LocalDateTime createdAt;

        public BatchInfo(Long id, String batchNumber, LocalDate productionDate, String shelfLife, LocalDateTime createdAt) {
            this.id = id;
            this.batchNumber = batchNumber;
            this.productionDate = productionDate;
            this.shelfLife = shelfLife;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public String getBatchNumber() { return batchNumber; }
        public LocalDate getProductionDate() { return productionDate; }
        public String getShelfLife() { return shelfLife; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    public static class MaterialInfo {
        private final String materialName;
        private final String batchNumber;
        private final String supplierName;
        private final String producerName;
        private final String producerAddress;
        private final String purchaseDate;

        public MaterialInfo(String materialName, String batchNumber, String supplierName,
                           String producerName, String producerAddress, String purchaseDate) {
            this.materialName = materialName;
            this.batchNumber = batchNumber;
            this.supplierName = supplierName;
            this.producerName = producerName;
            this.producerAddress = producerAddress;
            this.purchaseDate = purchaseDate;
        }

        public String getMaterialName() { return materialName; }
        public String getBatchNumber() { return batchNumber; }
        public String getSupplierName() { return supplierName; }
        public String getProducerName() { return producerName; }
        public String getProducerAddress() { return producerAddress; }
        public String getPurchaseDate() { return purchaseDate; }
    }

    public static class InspectionInfo {
        private final String sampleName;
        private final Integer sampleQuantity;
        private final String sampleSpecification;
        private final String imageUrl;
        private final String inspectorName;
        private final String inspectionTime;
        private final String resultStatus;
        private final String resultDetail;

        public InspectionInfo(String sampleName, Integer sampleQuantity, String sampleSpecification,
                              String imageUrl, String inspectorName, String inspectionTime,
                              String resultStatus, String resultDetail) {
            this.sampleName = sampleName;
            this.sampleQuantity = sampleQuantity;
            this.sampleSpecification = sampleSpecification;
            this.imageUrl = imageUrl;
            this.inspectorName = inspectorName;
            this.inspectionTime = inspectionTime;
            this.resultStatus = resultStatus;
            this.resultDetail = resultDetail;
        }

        public String getSampleName() { return sampleName; }
        public Integer getSampleQuantity() { return sampleQuantity; }
        public String getSampleSpecification() { return sampleSpecification; }
        public String getImageUrl() { return imageUrl; }
        public String getInspectorName() { return inspectorName; }
        public String getInspectionTime() { return inspectionTime; }
        public String getResultStatus() { return resultStatus; }
        public String getResultDetail() { return resultDetail; }
    }

    public static class StorageInfo {
        private final LocalDateTime storageTime;
        private final LocalDateTime outboundTime;
        private final String warehouseLocation;

        public StorageInfo(LocalDateTime storageTime, LocalDateTime outboundTime, String warehouseLocation) {
            this.storageTime = storageTime;
            this.outboundTime = outboundTime;
            this.warehouseLocation = warehouseLocation;
        }

        public LocalDateTime getStorageTime() { return storageTime; }
        public LocalDateTime getOutboundTime() { return outboundTime; }
        public String getWarehouseLocation() { return warehouseLocation; }
    }

    public static class TransportSaleInfo {
        private final LocalDateTime transportTime;
        private final String salesRegion;
        private final String transportCompany;

        public TransportSaleInfo(LocalDateTime transportTime, String salesRegion, String transportCompany) {
            this.transportTime = transportTime;
            this.salesRegion = salesRegion;
            this.transportCompany = transportCompany;
        }

        public LocalDateTime getTransportTime() { return transportTime; }
        public String getSalesRegion() { return salesRegion; }
        public String getTransportCompany() { return transportCompany; }
    }
}
