package com.foodtraceability.traceability.domain.aggregate;

import com.foodtraceability.entity.Inspection;
import com.foodtraceability.traceability.domain.event.DomainEvent;
import com.foodtraceability.traceability.domain.event.InspectionCompleted;
import com.foodtraceability.traceability.domain.vo.InspectionResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InspectionTest {

    @Test
    void create_setsFields() {
        Inspection i = Inspection.create(10L, "样本A", 5, "250ml/盒");
        assertEquals(10L, i.getBatchId());
        assertEquals("样本A", i.getSampleName());
        assertEquals(5, i.getSampleQuantity());
        assertEquals("250ml/盒", i.getSampleSpecification());
        assertNull(i.getId());
    }

    @Test
    void complete_qualified_setsResult() {
        Inspection i = Inspection.create(10L, "样本A", 5, "250ml/盒");
        i.setId(1L);

        i.complete(InspectionResult.pass(), null);

        assertEquals("合格", i.getResultStatus());
        assertNull(i.getResultDetail());
        assertTrue(i.isCompleted());
    }

    @Test
    void complete_qualified_setsInspectorNameAndTime() {
        Inspection i = Inspection.create(10L, "样本A", 5, "250ml/盒");
        i.setId(1L);

        i.complete(InspectionResult.pass(), "张三");

        assertEquals("合格", i.getResultStatus());
        assertEquals("张三", i.getInspectorName());
        assertNotNull(i.getInspectionTime());
        assertTrue(i.isCompleted());
    }

    @Test
    void complete_unqualified_setsResult() {
        Inspection i = Inspection.create(10L, "样本A", 5, "250ml/盒");
        i.setId(1L);

        i.complete(InspectionResult.fail("微生物超标"), null);

        assertEquals("不合格", i.getResultStatus());
        assertEquals("微生物超标", i.getResultDetail());
        assertTrue(i.isCompleted());
    }

    @Test
    void complete_registersEvent() {
        Inspection i = Inspection.create(10L, "样本A", 5, "250ml/盒");
        i.setId(1L);

        i.complete(InspectionResult.pass(), null);
        List<DomainEvent> events = i.pullEvents();

        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof InspectionCompleted);
        InspectionCompleted event = (InspectionCompleted) events.get(0);
        assertEquals(1L, event.inspectionId());
        assertEquals(10L, event.batchId());
        assertTrue(event.isQualified());
    }

    @Test
    void isCompleted_falseBeforeComplete() {
        Inspection i = Inspection.create(10L, "样本A", 5, "250ml/盒");
        assertFalse(i.isCompleted());
    }

    @Test
    void pullEvents_clearsList() {
        Inspection i = Inspection.create(10L, "样本A", 5, "250ml/盒");
        i.setId(1L);
        i.complete(InspectionResult.pass(), null);
        i.pullEvents();
        assertTrue(i.pullEvents().isEmpty());
    }
}
