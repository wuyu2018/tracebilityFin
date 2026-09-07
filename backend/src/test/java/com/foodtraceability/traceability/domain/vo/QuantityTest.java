package com.foodtraceability.traceability.domain.vo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuantityTest {

    @Test
    void of_validValues_succeeds() {
        Quantity q = Quantity.of(100.0, "盒");
        assertEquals(100.0, q.value());
        assertEquals("盒", q.unit());
    }

    @Test
    void of_negativeValue_throws() {
        assertThrows(IllegalArgumentException.class, () -> Quantity.of(-1.0, "盒"));
    }

    @Test
    void of_zeroValue_succeeds() {
        Quantity q = Quantity.of(0.0, "盒");
        assertTrue(q.isZero());
    }

    @Test
    void of_nullUnit_throws() {
        assertThrows(IllegalArgumentException.class, () -> Quantity.of(100.0, null));
    }

    @Test
    void of_blankUnit_throws() {
        assertThrows(IllegalArgumentException.class, () -> Quantity.of(100.0, " "));
    }

    @Test
    void equals_sameValueAndUnit_returnsTrue() {
        assertEquals(Quantity.of(100.0, "盒"), Quantity.of(100.0, "盒"));
    }

    @Test
    void equals_differentUnit_returnsFalse() {
        assertNotEquals(Quantity.of(100.0, "盒"), Quantity.of(100.0, "箱"));
    }

    @Test
    void equals_differentValue_returnsFalse() {
        assertNotEquals(Quantity.of(100.0, "盒"), Quantity.of(200.0, "盒"));
    }

    @Test
    void toString_returnsFormatted() {
        assertEquals("100.0 盒", Quantity.of(100.0, "盒").toString());
    }

    @Test
    void isZero_positive_false() {
        assertFalse(Quantity.of(1.0, "盒").isZero());
    }
}
