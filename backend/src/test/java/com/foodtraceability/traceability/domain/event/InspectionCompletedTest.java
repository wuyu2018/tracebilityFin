package com.foodtraceability.traceability.domain.event;

import com.foodtraceability.traceability.domain.vo.InspectionResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InspectionCompletedTest {

    @Test
    void create_setsFields() {
        InspectionResult r = InspectionResult.fail("不合格");
        InspectionCompleted event = new InspectionCompleted(1L, 10L, r);

        assertEquals(1L, event.inspectionId());
        assertEquals(10L, event.batchId());
        assertFalse(event.isQualified());
        assertSame(r, event.result());
        assertNotNull(event.occurredAt());
    }
}
