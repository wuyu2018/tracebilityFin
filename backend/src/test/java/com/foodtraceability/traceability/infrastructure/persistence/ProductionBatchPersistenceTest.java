package com.foodtraceability.traceability.infrastructure.persistence;

import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.repository.ProductionBatchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class ProductionBatchPersistenceTest {

    @Autowired
    private ProductionBatchRepository batchRepo;

    @Test
    void saveAndFindById() {
        ProductionBatch batch = new ProductionBatch();
        batch.setBatchNumber("B202604300001");
        batch.setProductId(1L);
        batch.setProductionDate(LocalDate.of(2026, 4, 30));
        batch.setShelfLife("12个月");
        batch.setQuantity(1000.0);
        batch.setUnit("盒");
        batch.setIsDeleted(false);

        ProductionBatch saved = batchRepo.save(batch);
        assertNotNull(saved.getId());

        Optional<ProductionBatch> found = batchRepo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("B202604300001", found.get().getBatchNumber());
        assertEquals(1L, found.get().getProductId());
        assertEquals(1000.0, found.get().getQuantity());
    }

    @Test
    void findByBatchNumberAndIsDeletedFalse() {
        ProductionBatch batch = new ProductionBatch();
        batch.setBatchNumber("B202604300002");
        batch.setProductId(1L);
        batch.setProductionDate(LocalDate.of(2026, 4, 30));
        batch.setShelfLife("6个月");
        batch.setQuantity(500.0);
        batch.setUnit("箱");
        batch.setIsDeleted(false);
        batchRepo.save(batch);

        Optional<ProductionBatch> found = batchRepo.findByBatchNumberAndIsDeletedFalse("B202604300002");
        assertTrue(found.isPresent());
        assertEquals("B202604300002", found.get().getBatchNumber());
    }

    @Test
    void softDelete_isReflectedInQuery() {
        ProductionBatch batch = new ProductionBatch();
        batch.setBatchNumber("B202604300003");
        batch.setProductId(1L);
        batch.setProductionDate(LocalDate.of(2026, 4, 30));
        batch.setShelfLife("12个月");
        batch.setQuantity(200.0);
        batch.setUnit("瓶");
        batch.setIsDeleted(false);
        batch = batchRepo.save(batch);

        batch.setIsDeleted(true);
        batchRepo.save(batch);

        Optional<ProductionBatch> notFound = batchRepo.findByBatchNumberAndIsDeletedFalse("B202604300003");
        assertTrue(notFound.isEmpty());
    }

    @Test
    void findDeletedBatchByFindById_stillReturns() {
        ProductionBatch batch = new ProductionBatch();
        batch.setBatchNumber("B202604300004");
        batch.setProductId(1L);
        batch.setProductionDate(LocalDate.of(2026, 4, 30));
        batch.setShelfLife("12个月");
        batch.setQuantity(100.0);
        batch.setUnit("袋");
        batch.setIsDeleted(false);
        batch = batchRepo.save(batch);

        batch.setIsDeleted(true);
        batchRepo.save(batch);

        assertTrue(batchRepo.findById(batch.getId()).isPresent());
    }

    @Test
    void createdAt_isSetOnPersist() {
        ProductionBatch batch = new ProductionBatch();
        batch.setBatchNumber("B202604300005");
        batch.setProductId(1L);
        batch.setProductionDate(LocalDate.of(2026, 4, 30));
        batch.setShelfLife("12个月");
        batch.setQuantity(1000.0);
        batch.setUnit("盒");
        batch.setIsDeleted(false);

        ProductionBatch saved = batchRepo.save(batch);
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void uniqueConstraint_productAndBatchNumber() {
        ProductionBatch batch1 = new ProductionBatch();
        batch1.setBatchNumber("B202604300001");
        batch1.setProductId(1L);
        batch1.setProductionDate(LocalDate.of(2026, 4, 30));
        batch1.setShelfLife("12个月");
        batch1.setQuantity(100.0);
        batch1.setUnit("盒");
        batch1.setIsDeleted(false);
        batchRepo.save(batch1);

        ProductionBatch batch2 = new ProductionBatch();
        batch2.setBatchNumber("B202604300001");
        batch2.setProductId(1L);
        batch2.setProductionDate(LocalDate.of(2026, 4, 30));
        batch2.setShelfLife("12个月");
        batch2.setQuantity(200.0);
        batch2.setUnit("箱");
        batch2.setIsDeleted(false);

        assertThrows(Exception.class, () -> batchRepo.saveAndFlush(batch2));
    }
}
