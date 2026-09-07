package com.foodtraceability.traceability.application.service;

import com.foodtraceability.entity.*;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraceabilityQueryApplicationServiceTest {

    @Mock private SecurityCodeRepository securityCodeRepo;
    @Mock private ProductionBatchRepository batchRepo;
    @Mock private ProductRepository productRepo;
    @Mock private BatchMaterialRelationRepository relationRepo;
    @Mock private MaterialPurchaseRepository materialPurchaseRepo;
    @Mock private MaterialRepository materialRepo;
    @Mock private InspectionRepository inspectionRepo;
    @Mock private StorageRepository storageRepo;
    @Mock private TransportSaleRepository transportSaleRepo;
    @Mock private TraceabilityLinkRepository linkRepo;

    private TraceabilityQueryApplicationService service;

    @BeforeEach
    void setUp() {
        service = new TraceabilityQueryApplicationService(
                securityCodeRepo, batchRepo, productRepo, relationRepo,
                materialPurchaseRepo, materialRepo, inspectionRepo,
                storageRepo, transportSaleRepo, linkRepo);
    }

    @Test
    void queryByCode_withValidCode_returnsFullTrace() {
        Product product = createProduct(1L);
        ProductionBatch batch = createBatch(10L, 1L);
        SecurityCode sc = createSecurityCode(100L, batch);
        MaterialPurchase mp = new MaterialPurchase();
        mp.setId(1000L);
        mp.setBatchNumber("MP-001");
        mp.setSupplierName("测试供应商");

        when(securityCodeRepo.findByCode("SC123")).thenReturn(Optional.of(sc));
        when(batchRepo.findById(10L)).thenReturn(Optional.of(batch));
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(relationRepo.findById_BatchId(10L)).thenReturn(List.of(createRelation(10L, 1000L)));
        when(materialPurchaseRepo.findById(1000L)).thenReturn(Optional.of(mp));
        when(inspectionRepo.findByBatch_Id(10L)).thenReturn(List.of());

        TraceabilityQueryApplicationService.TraceResult result = service.queryByCode("SC123");

        assertNotNull(result);
        assertEquals("已激活", result.status());
        assertEquals(1L, result.product().id());
        assertEquals(10L, result.batch().id());
        assertEquals(1, result.materials().size());
        assertNull(result.materials().get(0).materialName()); // no Material linked
        assertEquals("MP-001", result.materials().get(0).batchNumber());
        assertNull(result.inspection());
        assertNull(result.storage());
        assertNull(result.transportSale());

        verify(securityCodeRepo).save(sc);
    }

    @Test
    void queryByCode_withRepeatedQuery_setsRepeatedFlag() {
        ProductionBatch batch = createBatch(10L, 1L);
        SecurityCode sc = createSecurityCode(100L, batch);
        sc.setScanCount(2);
        sc.setFirstScanTime(LocalDateTime.of(2026, 4, 1, 10, 0));

        when(securityCodeRepo.findByCode("SC456")).thenReturn(Optional.of(sc));
        when(batchRepo.findById(10L)).thenReturn(Optional.of(batch));
        when(productRepo.findById(1L)).thenReturn(Optional.of(createProduct(1L)));
        when(relationRepo.findById_BatchId(10L)).thenReturn(List.of());
        when(inspectionRepo.findByBatch_Id(10L)).thenReturn(List.of());

        TraceabilityQueryApplicationService.TraceResult result = service.queryByCode("SC456");

        assertTrue(result.isRepeatedQuery());
        assertEquals(3, result.scanCount());
        assertEquals("2026-04-01T10:00", result.firstScanTime());
    }

    @Test
    void queryByCode_withFirstQuery_setsFirstScanTime() {
        ProductionBatch batch = createBatch(10L, 1L);
        SecurityCode sc = createSecurityCode(100L, batch);
        sc.setScanCount(0);

        when(securityCodeRepo.findByCode("SC789")).thenReturn(Optional.of(sc));
        when(batchRepo.findById(10L)).thenReturn(Optional.of(batch));
        when(productRepo.findById(1L)).thenReturn(Optional.of(createProduct(1L)));
        when(relationRepo.findById_BatchId(10L)).thenReturn(List.of());
        when(inspectionRepo.findByBatch_Id(10L)).thenReturn(List.of());

        TraceabilityQueryApplicationService.TraceResult result = service.queryByCode("SC789");

        assertFalse(result.isRepeatedQuery());
        assertEquals(1, sc.getScanCount());
        assertNotNull(sc.getFirstScanTime());
    }

    @Test
    void queryByCode_withInvalidCode_throws() {
        when(securityCodeRepo.findByCode("INVALID")).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.queryByCode("INVALID"));
    }

    @Test
    void queryByBatchNumber_withValidNumber_returnsTrace() {
        ProductionBatch batch = createBatch(10L, 1L);
        batch.setBatchNumber("B202604300001");
        when(batchRepo.findByBatchNumberAndIsDeletedFalse("B202604300001")).thenReturn(Optional.of(batch));
        when(productRepo.findById(1L)).thenReturn(Optional.of(createProduct(1L)));
        when(relationRepo.findById_BatchId(10L)).thenReturn(List.of());
        when(inspectionRepo.findByBatch_Id(10L)).thenReturn(List.of());

        TraceabilityQueryApplicationService.TraceResult result = service.queryByBatchNumber("B202604300001");

        assertNotNull(result);
        assertEquals("B202604300001", result.batch().batchNumber());
        assertEquals("未扫码", result.status());
        assertFalse(result.isRepeatedQuery());
    }

    @Test
    void queryByBatchNumber_withInvalidNumber_throws() {
        when(batchRepo.findByBatchNumberAndIsDeletedFalse("INVALID")).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.queryByBatchNumber("INVALID"));
    }

    @Test
    void queryByCode_withStorageAndTransport_includesThem() {
        ProductionBatch batch = createBatch(10L, 1L);
        batch.setStorageId(200L);
        batch.setTransportSaleId(300L);
        SecurityCode sc = createSecurityCode(100L, batch);
        Storage storage = new Storage();
        storage.setId(200L);
        storage.setWarehouseLocation("A区");
        TransportSale ts = new TransportSale();
        ts.setId(300L);
        ts.setTransportCompany("物流公司");

        when(securityCodeRepo.findByCode("SC999")).thenReturn(Optional.of(sc));
        when(batchRepo.findById(10L)).thenReturn(Optional.of(batch));
        when(productRepo.findById(1L)).thenReturn(Optional.of(createProduct(1L)));
        when(relationRepo.findById_BatchId(10L)).thenReturn(List.of());
        when(inspectionRepo.findByBatch_Id(10L)).thenReturn(List.of());
        when(storageRepo.findById(200L)).thenReturn(Optional.of(storage));
        when(transportSaleRepo.findById(300L)).thenReturn(Optional.of(ts));

        var result = service.queryByCode("SC999");

        assertNotNull(result.storage());
        assertEquals("A区", result.storage().warehouseLocation());
        assertNotNull(result.transportSale());
        assertEquals("物流公司", result.transportSale().transportCompany());
    }

    @Test
    void queryByCode_withInspection_includesIt() {
        ProductionBatch batch = createBatch(10L, 1L);
        SecurityCode sc = createSecurityCode(100L, batch);
        Inspection inspection = Inspection.create(batch.getId(), "样本A", 5, "规格A");

        when(securityCodeRepo.findByCode("SC111")).thenReturn(Optional.of(sc));
        when(batchRepo.findById(10L)).thenReturn(Optional.of(batch));
        when(productRepo.findById(1L)).thenReturn(Optional.of(createProduct(1L)));
        when(relationRepo.findById_BatchId(10L)).thenReturn(List.of());
        when(inspectionRepo.findByBatch_Id(10L)).thenReturn(List.of(inspection));

        var result = service.queryByCode("SC111");

        assertNotNull(result.inspection());
        assertEquals("样本A", result.inspection().sampleName());
    }

    @Test
    void queryByCode_withMaterial_includeMaterialInfo() {
        ProductionBatch batch = createBatch(10L, 1L);
        SecurityCode sc = createSecurityCode(100L, batch);
        Material material = new Material();
        material.setId(10L);
        material.setName("有机原料");
        MaterialPurchase mp = new MaterialPurchase();
        mp.setId(1000L);
        mp.setMaterial(material);
        mp.setBatchNumber("MP-BATCH-001");
        mp.setSupplierName("供应商A");
        mp.setProducerName("生产商A");

        when(securityCodeRepo.findByCode("SC222")).thenReturn(Optional.of(sc));
        when(batchRepo.findById(10L)).thenReturn(Optional.of(batch));
        when(productRepo.findById(1L)).thenReturn(Optional.of(createProduct(1L)));
        when(relationRepo.findById_BatchId(10L)).thenReturn(List.of(createRelation(10L, 1000L)));
        when(materialPurchaseRepo.findById(1000L)).thenReturn(Optional.of(mp));
        when(inspectionRepo.findByBatch_Id(10L)).thenReturn(List.of());

        var result = service.queryByCode("SC222");

        assertEquals(1, result.materials().size());
        assertEquals("有机原料", result.materials().get(0).materialName());
        assertEquals("MP-BATCH-001", result.materials().get(0).batchNumber());
    }

    private Product createProduct(Long id) {
        Product p = new Product();
        p.setId(id);
        p.setName("测试产品");
        p.setShelfLife("12个月");
        return p;
    }

    private ProductionBatch createBatch(Long id, Long productId) {
        ProductionBatch b = new ProductionBatch();
        b.setId(id);
        b.setProductId(productId);
        b.setBatchNumber("B" + id);
        b.setProductionDate(LocalDate.now());
        b.setShelfLife("12个月");
        b.setQuantity(100.0);
        b.setUnit("盒");
        return b;
    }

    private SecurityCode createSecurityCode(Long id, ProductionBatch batch) {
        SecurityCode sc = new SecurityCode();
        sc.setId(id);
        sc.setCode("SC" + id);
        sc.setBatch(batch);
        sc.setStatus("SC123-STATUS");
        sc.setScanCount(0);
        return sc;
    }

    private BatchMaterialRelation createRelation(Long batchId, Long mpId) {
        return BatchMaterialRelation.create(batchId, mpId);
    }
}
