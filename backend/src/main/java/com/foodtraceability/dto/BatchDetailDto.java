package com.foodtraceability.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BatchDetailDto {

    private BatchInfo batch;
    private ProductInfo product;
    private List<MaterialInfo> materials;
    private InspectionInfo inspection;
    private StorageInfo storage;
    private TransportInfo transport;
    private CodeStats codes;

    public BatchInfo getBatch() { return batch; }
    public void setBatch(BatchInfo batch) { this.batch = batch; }
    public ProductInfo getProduct() { return product; }
    public void setProduct(ProductInfo product) { this.product = product; }
    public List<MaterialInfo> getMaterials() { return materials; }
    public void setMaterials(List<MaterialInfo> materials) { this.materials = materials; }
    public InspectionInfo getInspection() { return inspection; }
    public void setInspection(InspectionInfo inspection) { this.inspection = inspection; }
    public StorageInfo getStorage() { return storage; }
    public void setStorage(StorageInfo storage) { this.storage = storage; }
    public TransportInfo getTransport() { return transport; }
    public void setTransport(TransportInfo transport) { this.transport = transport; }
    public CodeStats getCodes() { return codes; }
    public void setCodes(CodeStats codes) { this.codes = codes; }

    public static class BatchInfo {
        private Long id;
        private String batchNumber;
        private LocalDate productionDate;
        private String shelfLife;
        private Double quantity;
        private String unit;
        private LocalDateTime createdAt;

        public BatchInfo() {}
        public BatchInfo(Long id, String batchNumber, LocalDate productionDate, String shelfLife,
                         Double quantity, String unit, LocalDateTime createdAt) {
            this.id = id; this.batchNumber = batchNumber; this.productionDate = productionDate;
            this.shelfLife = shelfLife; this.quantity = quantity; this.unit = unit; this.createdAt = createdAt;
        }
        public Long getId() { return id; }
        public String getBatchNumber() { return batchNumber; }
        public LocalDate getProductionDate() { return productionDate; }
        public String getShelfLife() { return shelfLife; }
        public Double getQuantity() { return quantity; }
        public String getUnit() { return unit; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    public static class ProductInfo {
        private Long id;
        private String name;
        private String specification;
        private String shelfLife;
        private String contactPhone;
        private String contactEmail;

        public ProductInfo() {}
        public ProductInfo(Long id, String name, String specification, String shelfLife,
                           String contactPhone, String contactEmail) {
            this.id = id; this.name = name; this.specification = specification;
            this.shelfLife = shelfLife; this.contactPhone = contactPhone; this.contactEmail = contactEmail;
        }
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getSpecification() { return specification; }
        public String getShelfLife() { return shelfLife; }
        public String getContactPhone() { return contactPhone; }
        public String getContactEmail() { return contactEmail; }
    }

    public static class MaterialInfo {
        private String materialName;
        private String batchNumber;
        private String supplierName;
        private String producerName;
        private String producerAddress;
        private LocalDateTime purchaseDate;
        private Double quantity;
        private String unit;

        public MaterialInfo() {}
        public MaterialInfo(String materialName, String batchNumber, String supplierName,
                            String producerName, String producerAddress, LocalDateTime purchaseDate,
                            Double quantity, String unit) {
            this.materialName = materialName; this.batchNumber = batchNumber;
            this.supplierName = supplierName; this.producerName = producerName;
            this.producerAddress = producerAddress; this.purchaseDate = purchaseDate;
            this.quantity = quantity; this.unit = unit;
        }
        public String getMaterialName() { return materialName; }
        public String getBatchNumber() { return batchNumber; }
        public String getSupplierName() { return supplierName; }
        public String getProducerName() { return producerName; }
        public String getProducerAddress() { return producerAddress; }
        public LocalDateTime getPurchaseDate() { return purchaseDate; }
        public Double getQuantity() { return quantity; }
        public String getUnit() { return unit; }
    }

    public static class InspectionInfo {
        private String sampleName;
        private Integer sampleQuantity;
        private String sampleSpecification;
        private String imageUrl;
        private String inspectorName;
        private LocalDateTime inspectionTime;
        private String resultStatus;
        private String resultDetail;

        public InspectionInfo() {}
        public InspectionInfo(String sampleName, Integer sampleQuantity, String sampleSpecification,
                              String imageUrl, String inspectorName, LocalDateTime inspectionTime,
                              String resultStatus, String resultDetail) {
            this.sampleName = sampleName; this.sampleQuantity = sampleQuantity;
            this.sampleSpecification = sampleSpecification; this.imageUrl = imageUrl;
            this.inspectorName = inspectorName; this.inspectionTime = inspectionTime;
            this.resultStatus = resultStatus; this.resultDetail = resultDetail;
        }
        public String getSampleName() { return sampleName; }
        public Integer getSampleQuantity() { return sampleQuantity; }
        public String getSampleSpecification() { return sampleSpecification; }
        public String getImageUrl() { return imageUrl; }
        public String getInspectorName() { return inspectorName; }
        public LocalDateTime getInspectionTime() { return inspectionTime; }
        public String getResultStatus() { return resultStatus; }
        public String getResultDetail() { return resultDetail; }
    }

    public static class StorageInfo {
        private LocalDateTime storageTime;
        private LocalDateTime outboundTime;
        private String warehouseLocation;
        private Double quantity;
        private String unit;

        public StorageInfo() {}
        public StorageInfo(LocalDateTime storageTime, LocalDateTime outboundTime,
                           String warehouseLocation, Double quantity, String unit) {
            this.storageTime = storageTime; this.outboundTime = outboundTime;
            this.warehouseLocation = warehouseLocation; this.quantity = quantity; this.unit = unit;
        }
        public LocalDateTime getStorageTime() { return storageTime; }
        public LocalDateTime getOutboundTime() { return outboundTime; }
        public String getWarehouseLocation() { return warehouseLocation; }
        public Double getQuantity() { return quantity; }
        public String getUnit() { return unit; }
    }

    public static class TransportInfo {
        private String transportCompany;
        private String vehicleNumber;
        private LocalDateTime time;
        private String salesRegion;
        private String receiverName;
        private String receiverContact;
        private Double environmentTemperature;
        private Double productTemperature;
        private String recorderName;

        public TransportInfo() {}
        public TransportInfo(String transportCompany, String vehicleNumber, LocalDateTime time,
                             String salesRegion, String receiverName, String receiverContact,
                             Double environmentTemperature, Double productTemperature, String recorderName) {
            this.transportCompany = transportCompany; this.vehicleNumber = vehicleNumber;
            this.time = time; this.salesRegion = salesRegion;
            this.receiverName = receiverName; this.receiverContact = receiverContact;
            this.environmentTemperature = environmentTemperature;
            this.productTemperature = productTemperature; this.recorderName = recorderName;
        }
        public String getTransportCompany() { return transportCompany; }
        public String getVehicleNumber() { return vehicleNumber; }
        public LocalDateTime getTime() { return time; }
        public String getSalesRegion() { return salesRegion; }
        public String getReceiverName() { return receiverName; }
        public String getReceiverContact() { return receiverContact; }
        public Double getEnvironmentTemperature() { return environmentTemperature; }
        public Double getProductTemperature() { return productTemperature; }
        public String getRecorderName() { return recorderName; }
    }

    public static class CodeStats {
        private long total;
        private long inactive;
        private long active;
        private long frozen;

        public CodeStats() {}
        public CodeStats(long total, long inactive, long active, long frozen) {
            this.total = total; this.inactive = inactive; this.active = active; this.frozen = frozen;
        }
        public long getTotal() { return total; }
        public long getInactive() { return inactive; }
        public long getActive() { return active; }
        public long getFrozen() { return frozen; }
    }
}
