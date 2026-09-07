package com.foodtraceability.traceability.application.service;

import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.entity.Storage;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.repository.StorageRepository;
import com.foodtraceability.repository.TraceabilityLinkRepository;
import com.foodtraceability.traceability.application.dto.RecordStorageRequest;
import com.foodtraceability.traceability.application.dto.RecordStorageResponse;
import com.foodtraceability.traceability.infrastructure.messaging.DomainEventPublisherImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageApplicationServiceTest {

    @Mock private StorageRepository storageRepo;
    @Mock private ProductionBatchRepository batchRepo;
    @Mock private TraceabilityLinkRepository linkRepo;
    @Mock private DomainEventPublisherImpl eventPublisher;

    private StorageApplicationService service;

    @Captor private ArgumentCaptor<Storage> storageCaptor;

    @BeforeEach
    void setUp() {
        service = new StorageApplicationService(storageRepo, batchRepo, linkRepo, eventPublisher);
    }

    @Test
    void recordStorage_withValidBatch_savesAndUpdatesBatch() {
        ProductionBatch batch = new ProductionBatch();
        batch.setId(10L);
        Storage saved = new Storage();
        saved.setId(1L);
        saved.setBatchId(10L);

        when(batchRepo.findById(10L)).thenReturn(Optional.of(batch));
        when(storageRepo.save(any(Storage.class))).thenReturn(saved);

        RecordStorageResponse result = service.recordStorage(
                new RecordStorageRequest(10L, LocalDateTime.now(), null, 100.0, "箱", "A区"));

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(10L, result.batchId());
        verify(batchRepo, times(1)).save(batch);
        assertEquals(1L, batch.getStorageId());
    }

    @Test
    void recordStorage_invalidBatch_throws() {
        when(batchRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> service.recordStorage(
                        new RecordStorageRequest(999L, LocalDateTime.now(), null, 100.0, "箱", "A区")));
        verify(storageRepo, never()).save(any());
    }
}
