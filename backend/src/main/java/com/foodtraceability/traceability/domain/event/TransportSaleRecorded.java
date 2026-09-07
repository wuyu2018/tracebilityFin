package com.foodtraceability.traceability.domain.event;

import java.time.Instant;
import java.time.LocalDateTime;

public class TransportSaleRecorded implements DomainEvent {
    private final Instant occurredAt;
    private final Long transportSaleId;
    private final Long batchId;
    private final LocalDateTime time;
    private final String transportCompany;
    private final String salesRegion;

    public TransportSaleRecorded(Long transportSaleId, Long batchId, LocalDateTime time,
                                  String transportCompany, String salesRegion) {
        this.occurredAt = Instant.now();
        this.transportSaleId = transportSaleId;
        this.batchId = batchId;
        this.time = time;
        this.transportCompany = transportCompany;
        this.salesRegion = salesRegion;
    }

    @Override
    public Instant occurredAt() { return occurredAt; }

    public Long transportSaleId() { return transportSaleId; }
    public Long batchId() { return batchId; }
    public LocalDateTime time() { return time; }
    public String transportCompany() { return transportCompany; }
    public String salesRegion() { return salesRegion; }
}
