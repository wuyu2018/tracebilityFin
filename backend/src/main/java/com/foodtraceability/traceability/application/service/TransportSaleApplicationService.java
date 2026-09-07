package com.foodtraceability.traceability.application.service;

import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.entity.TraceabilityLink;
import com.foodtraceability.entity.TransportSale;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.repository.TraceabilityLinkRepository;
import com.foodtraceability.repository.TransportSaleRepository;
import com.foodtraceability.traceability.domain.event.TransportSaleRecorded;
import com.foodtraceability.traceability.infrastructure.messaging.DomainEventPublisherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransportSaleApplicationService {

    private static final Logger log = LoggerFactory.getLogger(TransportSaleApplicationService.class);

    private final TransportSaleRepository transportSaleRepo;
    private final ProductionBatchRepository batchRepo;
    private final TraceabilityLinkRepository linkRepo;
    private final DomainEventPublisherImpl eventPublisher;

    public TransportSaleApplicationService(TransportSaleRepository transportSaleRepo,
                                            ProductionBatchRepository batchRepo,
                                            TraceabilityLinkRepository linkRepo,
                                            DomainEventPublisherImpl eventPublisher) {
        this.transportSaleRepo = transportSaleRepo;
        this.batchRepo = batchRepo;
        this.linkRepo = linkRepo;
        this.eventPublisher = eventPublisher;
    }

    public record RecordTransportSaleRequest(Long batchId, Double environmentTemperature,
                                              Double productTemperature, LocalDateTime time,
                                              String transportCompany, String vehicleNumber,
                                              String salesRegion, String receiverName,
                                              String receiverContact,
                                              String recorderName) {}

    public record RecordTransportSaleResponse(Long id, Long batchId, String transportCompany, String salesRegion) {}

    public record TransportSaleListResponse(Long id, Long batchId, String transportCompany,
                                              String vehicleNumber, LocalDateTime time,
                                              String salesRegion, String receiverName,
                                              String receiverContact,
                                              Double environmentTemperature,
                                              Double productTemperature,
                                              String recorderName,
                                              String batchNumber, String productName) {}

    public RecordTransportSaleResponse recordTransportSale(RecordTransportSaleRequest req) {
        if (req.batchId() == null) {
            throw new BusinessException("批次不能为空");
        }
        ProductionBatch batch = batchRepo.findById(req.batchId())
                .orElseThrow(() -> new BusinessException("批次不存在: " + req.batchId()));

        TransportSale ts = new TransportSale();
        ts.associateBatch(batch);
        ts.setEnvironmentTemperature(req.environmentTemperature());
        ts.setProductTemperature(req.productTemperature());
        ts.setTime(LocalDateTime.now());
        ts.setRecorderName(req.recorderName());
        ts.setTransportCompany(req.transportCompany());
        ts.setVehicleNumber(req.vehicleNumber());
        ts.setSalesRegion(req.salesRegion());
        ts.setReceiverName(req.receiverName());
        ts.setReceiverContact(req.receiverContact());
        ts = transportSaleRepo.save(ts);

        Long tsId = ts.getId();
        batch.associateTransportSale(ts);
        batchRepo.save(batch);

        if (!linkRepo.existsByBatchIdAndEntityTypeAndEntityId(req.batchId(), "TRANSPORT_SALE", tsId)) {
            linkRepo.save(TraceabilityLink.create(req.batchId(), "TRANSPORT_SALE", tsId));
        }

        var event = new TransportSaleRecorded(tsId, req.batchId(), req.time(),
                req.transportCompany(), req.salesRegion());
        publishAfterCommit(event);

        log.info("TransportSale recorded: id={}, batchId={}", tsId, req.batchId());
        return new RecordTransportSaleResponse(tsId, req.batchId(), req.transportCompany(), req.salesRegion());
    }

    @Transactional(readOnly = true)
    public List<TransportSaleListResponse> listTransportSales() {
        List<TransportSale> sales = transportSaleRepo.findAll();
        Set<Long> batchIds = sales.stream().map(TransportSale::getBatchId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, ProductionBatch> batchMap = batchRepo.findAllById(batchIds).stream()
                .collect(Collectors.toMap(ProductionBatch::getId, b -> b));
        return sales.stream()
                .map(t -> {
                    ProductionBatch b = batchMap.get(t.getBatchId());
                    String bn = b != null ? b.getBatchNumber() : null;
                    String pn = b != null && b.getProduct() != null ? b.getProduct().getName() : null;
                    return new TransportSaleListResponse(t.getId(), t.getBatchId(),
                            t.getTransportCompany(), t.getVehicleNumber(), t.getTime(),
                            t.getSalesRegion(), t.getReceiverName(), t.getReceiverContact(),
                            t.getEnvironmentTemperature(), t.getProductTemperature(),
                            t.getRecorderName(), bn, pn);
                })
                .toList();
    }

    private void publishAfterCommit(TransportSaleRecorded event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            eventPublisher.publish(event);
                        }
                    });
        } else {
            eventPublisher.publish(event);
        }
    }
}
