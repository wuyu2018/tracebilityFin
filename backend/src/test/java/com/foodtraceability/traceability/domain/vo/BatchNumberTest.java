package com.foodtraceability.traceability.domain.vo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BatchNumberTest {

    @Test
    void of_validFormat_succeeds() {
        BatchNumber bn = BatchNumber.of("B202604300001");
        assertEquals("B202604300001", bn.value());
    }

    @Test
    void of_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> BatchNumber.of(null));
    }

    @Test
    void of_invalidPrefix_throws() {
        assertThrows(IllegalArgumentException.class, () -> BatchNumber.of("X202604300001"));
    }

    @Test
    void of_shortSequence_throws() {
        assertThrows(IllegalArgumentException.class, () -> BatchNumber.of("B20260430001"));
    }

    @Test
    void of_missingDate_throws() {
        assertThrows(IllegalArgumentException.class, () -> BatchNumber.of("B0001"));
    }

    @Test
    void generate_createsValidFormat() {
        BatchNumber bn = BatchNumber.generate("20260430", 1);
        assertEquals("B202604300001", bn.value());
    }

    @Test
    void generate_padsSequenceToFourDigits() {
        BatchNumber bn = BatchNumber.generate("20260430", 9999);
        assertEquals("B202604309999", bn.value());
    }

    @Test
    void equals_sameValue_returnsTrue() {
        BatchNumber a = BatchNumber.of("B202604300001");
        BatchNumber b = BatchNumber.of("B202604300001");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_differentValue_returnsFalse() {
        BatchNumber a = BatchNumber.of("B202604300001");
        BatchNumber b = BatchNumber.of("B202604300002");
        assertNotEquals(a, b);
    }

    @Test
    void equals_null_returnsFalse() {
        BatchNumber a = BatchNumber.of("B202604300001");
        assertNotEquals(null, a);
    }

    @Test
    void toString_returnsValue() {
        BatchNumber bn = BatchNumber.of("B202604300001");
        assertEquals("B202604300001", bn.toString());
    }
}
