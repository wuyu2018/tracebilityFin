package com.foodtraceability.traceability.application.service;

import com.foodtraceability.entity.Inspection;
import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.InspectionRepository;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.repository.TraceabilityLinkRepository;
import com.foodtraceability.traceability.application.dto.CompleteInspectionRequest;
import com.foodtraceability.traceability.application.dto.CompleteInspectionResponse;
import com.foodtraceability.traceability.infrastructure.messaging.DomainEventPublisherImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionApplicationServiceTest {

    @Mock private InspectionRepository inspectionRepo;
    @Mock private ProductionBatchRepository batchRepo;
    @Mock private TraceabilityLinkRepository linkRepo;
    @Mock private DomainEventPublisherImpl eventPublisher;

    private InspectionApplicationService service;

    @BeforeEach
    void setUp() {
        service = new InspectionApplicationService(inspectionRepo, batchRepo, linkRepo, eventPublisher);
    }

    @Test
    void completeInspection_qualified_savesAndReturns() {
        ProductionBatch batch = new ProductionBatch();
        batch.setId(10L);

        when(batchRepo.findById(10L)).thenReturn(Optional.of(batch));
        when(inspectionRepo.save(any(Inspection.class))).thenAnswer(invocation -> {
            Inspection i = invocation.getArgument(0);
            i.setId(1L);
            return i;
        });

        CompleteInspectionResponse result = service.completeInspection(
                new CompleteInspectionRequest(10L, "样本A", 5, "250ml", null, true, null, null));

        assertNotNull(result);
        assertEquals("合格", result.resultStatus());
        assertEquals(10L, result.batchId());
    }

    @Test
    void completeInspection_unqualified_savesAndReturns() {
        ProductionBatch batch = new ProductionBatch();
        batch.setId(20L);

        when(batchRepo.findById(20L)).thenReturn(Optional.of(batch));
        when(inspectionRepo.save(any(Inspection.class))).thenAnswer(invocation -> {
            Inspection i = invocation.getArgument(0);
            i.setId(2L);
            return i;
        });

        CompleteInspectionResponse result = service.completeInspection(
                new CompleteInspectionRequest(20L, "样本B", 3, "500ml", null, false, "微生物超标", null));

        assertEquals("不合格", result.resultStatus());
    }

    @Test
    void completeInspection_invalidBatch_throws() {
        when(batchRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> service.completeInspection(
                        new CompleteInspectionRequest(999L, "x", 1, "x", null, true, null, null)));
        verify(inspectionRepo, never()).save(any());
    }
}
