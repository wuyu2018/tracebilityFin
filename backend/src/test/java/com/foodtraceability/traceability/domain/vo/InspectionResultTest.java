package com.foodtraceability.traceability.domain.vo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InspectionResultTest {

    @Test
    void pass_createsQualified() {
        InspectionResult r = InspectionResult.pass();
        assertTrue(r.isQualified());
        assertEquals("合格", r.displayStatus());
        assertNull(r.detail());
    }

    @Test
    void fail_createsUnqualified() {
        InspectionResult r = InspectionResult.fail("微生物超标");
        assertFalse(r.isQualified());
        assertEquals("不合格", r.displayStatus());
        assertEquals("微生物超标", r.detail());
    }

    @Test
    void fail_blankReason_throws() {
        assertThrows(IllegalArgumentException.class, () -> InspectionResult.fail(" "));
    }

    @Test
    void fail_nullReason_throws() {
        assertThrows(IllegalArgumentException.class, () -> InspectionResult.fail(null));
    }

    @Test
    void equals_sameValues() {
        assertEquals(InspectionResult.pass(), InspectionResult.pass());
        assertEquals(InspectionResult.fail("x"), InspectionResult.fail("x"));
    }

    @Test
    void equals_differentValues() {
        assertNotEquals(InspectionResult.pass(), InspectionResult.fail("x"));
    }
}
