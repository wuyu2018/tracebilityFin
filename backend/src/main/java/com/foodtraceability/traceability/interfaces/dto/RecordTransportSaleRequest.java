package com.foodtraceability.traceability.interfaces.dto;

import com.foodtraceability.traceability.application.service.TransportSaleApplicationService;

import java.time.LocalDateTime;

public class RecordTransportSaleRequest {

    private Long batchId;
    private Double environmentTemperature;
    private Double productTemperature;
    private LocalDateTime time;
    private String transportCompany;
    private String vehicleNumber;
    private String salesRegion;
    private String receiverName;
    private String receiverContact;
    private String recorderName;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Double getEnvironmentTemperature() { return environmentTemperature; }
    public void setEnvironmentTemperature(Double environmentTemperature) { this.environmentTemperature = environmentTemperature; }
    public Double getProductTemperature() { return productTemperature; }
    public void setProductTemperature(Double productTemperature) { this.productTemperature = productTemperature; }
    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }
    public String getTransportCompany() { return transportCompany; }
    public void setTransportCompany(String transportCompany) { this.transportCompany = transportCompany; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getSalesRegion() { return salesRegion; }
    public void setSalesRegion(String salesRegion) { this.salesRegion = salesRegion; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverContact() { return receiverContact; }
    public void setReceiverContact(String receiverContact) { this.receiverContact = receiverContact; }
    public String getRecorderName() { return recorderName; }
    public void setRecorderName(String recorderName) { this.recorderName = recorderName; }

    public TransportSaleApplicationService.RecordTransportSaleRequest toAppRequest() {
        return new TransportSaleApplicationService.RecordTransportSaleRequest(
                batchId, environmentTemperature, productTemperature, time,
                transportCompany, vehicleNumber, salesRegion, receiverName, receiverContact,
                recorderName);
    }
}
