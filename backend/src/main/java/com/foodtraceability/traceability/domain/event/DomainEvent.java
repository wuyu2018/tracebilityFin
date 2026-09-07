package com.foodtraceability.traceability.domain.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
}
