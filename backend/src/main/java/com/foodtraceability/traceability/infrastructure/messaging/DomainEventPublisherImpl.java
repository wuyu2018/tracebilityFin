package com.foodtraceability.traceability.infrastructure.messaging;

import com.foodtraceability.traceability.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DomainEventPublisherImpl {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisherImpl.class);

    private final ApplicationEventPublisher springPublisher;

    public DomainEventPublisherImpl(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    public void publish(DomainEvent event) {
        log.debug("Publishing domain event: {} (at {})", event.getClass().getSimpleName(), event.occurredAt());
        springPublisher.publishEvent(event);
    }
}
