package com.foodtraceability.traceability.domain.event;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class GoodsReceivedTest {

    @Test
    void create_setsFields() {
        LocalDateTime now = LocalDateTime.of(2026, 4, 30, 10, 0);
        GoodsReceived event = new GoodsReceived(1L, 10L, now);

        assertEquals(1L, event.storageId());
        assertEquals(10L, event.batchId());
        assertEquals(now, event.storageTime());
        assertNotNull(event.occurredAt());
        assertTrue(event.occurredAt() instanceof Instant);
    }
}
