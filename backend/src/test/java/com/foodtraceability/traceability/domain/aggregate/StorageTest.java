package com.foodtraceability.traceability.domain.aggregate;

import com.foodtraceability.entity.Storage;
import com.foodtraceability.traceability.domain.event.DomainEvent;
import com.foodtraceability.traceability.domain.event.GoodsReceived;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StorageTest {

    @Test
    void create_setsAllFields() {
        LocalDateTime now = LocalDateTime.of(2026, 4, 30, 10, 0);
        Storage s = Storage.create(10L, now, null, 1000.0, "箱", "A区-01库位");

        assertEquals(10L, s.getBatchId());
        assertEquals(now, s.getStorageTime());
        assertEquals(1000.0, s.getQuantity());
        assertEquals("箱", s.getUnit());
        assertEquals("A区-01库位", s.getWarehouseLocation());
        assertNull(s.getId());
        assertNull(s.getOutboundTime());
    }

    @Test
    void registerAndPullEvents() {
        Storage s = Storage.create(10L, LocalDateTime.now(), null, 100.0, "盒", "B区");
        GoodsReceived event = new GoodsReceived(1L, 10L, LocalDateTime.now());

        s.registerEvent(event);
        List<DomainEvent> pulled = s.pullEvents();

        assertEquals(1, pulled.size());
        assertSame(event, pulled.get(0));
        assertTrue(s.pullEvents().isEmpty());
    }

    @Test
    void associateBatch_setsBatchId() {
        Storage s = Storage.create(10L, LocalDateTime.now(), null, 100.0, "盒", "C区");
        com.foodtraceability.entity.ProductionBatch batch = new com.foodtraceability.entity.ProductionBatch();
        batch.setId(99L);

        s.associateBatch(batch);

        assertEquals(99L, s.getBatchId());
    }
}
