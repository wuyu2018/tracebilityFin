package com.foodtraceability.traceability.domain.aggregate;

import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.entity.Storage;
import com.foodtraceability.entity.TransportSale;
import com.foodtraceability.traceability.domain.vo.BatchNumber;
import com.foodtraceability.traceability.domain.vo.Quantity;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProductionBatchTest {

    @Test
    void create_setsAllFields() {
        BatchNumber batchNo = BatchNumber.of("B202604300001");
        Quantity qty = Quantity.of(1000.0, "盒");
        LocalDate prodDate = LocalDate.of(2026, 4, 30);

        ProductionBatch batch = ProductionBatch.create(batchNo, 1L, prodDate, "12个月", qty);

        assertEquals("B202604300001", batch.getBatchNumber());
        assertEquals(1L, batch.getProductId());
        assertEquals(prodDate, batch.getProductionDate());
        assertEquals("12个月", batch.getShelfLife());
        assertEquals(1000.0, batch.getQuantity());
        assertEquals("盒", batch.getUnit());
        assertFalse(batch.isDeleted());
    }

    @Test
    void create_defaultIsDeletedFalse() {
        ProductionBatch batch = ProductionBatch.create(
                BatchNumber.of("B202604300001"), 1L, LocalDate.now(), "12个月", Quantity.of(100.0, "盒"));
        assertFalse(batch.getIsDeleted());
    }

    @Test
    void softDelete_marksDeleted() {
        ProductionBatch batch = ProductionBatch.create(
                BatchNumber.of("B202604300001"), 1L, LocalDate.now(), "12个月", Quantity.of(100.0, "盒"));
        batch.softDelete();
        assertTrue(batch.isDeleted());
        assertTrue(batch.getIsDeleted());
    }

    @Test
    void isDeleted_returnsTrue_whenTrue() {
        ProductionBatch batch = ProductionBatch.create(
                BatchNumber.of("B202604300001"), 1L, LocalDate.now(), "12个月", Quantity.of(100.0, "盒"));
        batch.setIsDeleted(true);
        assertTrue(batch.isDeleted());
    }

    @Test
    void isDeleted_returnsFalse_whenNull() {
        ProductionBatch batch = ProductionBatch.create(
                BatchNumber.of("B202604300001"), 1L, LocalDate.now(), "12个月", Quantity.of(100.0, "盒"));
        batch.setIsDeleted(null);
        assertFalse(batch.isDeleted());
    }

    @Test
    void associateStorage_setsStorageId() {
        ProductionBatch batch = ProductionBatch.create(
                BatchNumber.of("B202604300001"), 1L, LocalDate.now(), "12个月", Quantity.of(100.0, "盒"));
        Storage storage = new Storage();
        storage.setId(99L);

        batch.associateStorage(storage);

        assertEquals(99L, batch.getStorageId());
    }

    @Test
    void associateTransportSale_setsTransportSaleId() {
        ProductionBatch batch = ProductionBatch.create(
                BatchNumber.of("B202604300001"), 1L, LocalDate.now(), "12个月", Quantity.of(100.0, "盒"));
        TransportSale transportSale = new TransportSale();
        transportSale.setId(88L);

        batch.associateTransportSale(transportSale);

        assertEquals(88L, batch.getTransportSaleId());
    }

    @Test
    void create_withZeroQuantity_succeeds() {
        ProductionBatch batch = ProductionBatch.create(
                BatchNumber.of("B202604300001"), 1L, LocalDate.now(), "12个月", Quantity.of(0.0, "盒"));
        assertEquals(0.0, batch.getQuantity());
    }
}
