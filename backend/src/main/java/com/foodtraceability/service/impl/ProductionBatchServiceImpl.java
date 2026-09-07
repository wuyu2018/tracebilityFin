package com.foodtraceability.service.impl;

import com.foodtraceability.dto.ProductionBatchDTO;
import com.foodtraceability.dto.StorageDTO;
import com.foodtraceability.dto.TransportSaleDTO;
import com.foodtraceability.entity.Storage;
import com.foodtraceability.entity.TransportSale;
import com.foodtraceability.entity.Product;
import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.repository.StorageRepository;
import com.foodtraceability.repository.TransportSaleRepository;
import com.foodtraceability.service.ProductionBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ProductionBatchServiceImpl implements ProductionBatchService {
    private static final Logger log = LoggerFactory.getLogger(ProductionBatchServiceImpl.class);

    private final ProductionBatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final StorageRepository storageRepository;
    private final TransportSaleRepository transportSaleRepository;

    @Autowired
    public ProductionBatchServiceImpl(ProductionBatchRepository batchRepository,
                                      ProductRepository productRepository,
                                      StorageRepository storageRepository,
                                      TransportSaleRepository transportSaleRepository) {
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
        this.storageRepository = storageRepository;
        this.transportSaleRepository = transportSaleRepository;
    }

    private long nextBatchSeq() {
        return batchRepository.findByIsDeletedFalse().stream()
                .map(ProductionBatch::getBatchNumber)
                .filter(n -> n != null && n.matches("B\\d{8}\\d{4}"))
                .map(n -> n.substring(9))
                .mapToLong(Long::parseLong)
                .max()
                .orElse(0) + 1;
    }

    @Override
    @Transactional
    public ProductionBatch updateBatch(Long id, ProductionBatchDTO dto) {
        ProductionBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));

        updateBatchFields(batch, dto);
        updateStorage(batch, dto.getStorage());
        updateTransportSale(batch, dto.getTransportSale());

        return batchRepository.save(batch);
    }

    private void updateBatchFields(ProductionBatch batch, ProductionBatchDTO dto) {
        if (dto.getProductId() != null) {
            productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("产品不存在"));
            batch.setProductId(dto.getProductId());
        }
        batch.changeProductionDate(dto.getProductionDate());
        batch.changeShelfLife(dto.getShelfLife());
        batch.changeQuantity(dto.getQuantity());
        batch.changeUnit(dto.getUnit());
    }

    private void updateStorage(ProductionBatch batch, StorageDTO storageDto) {
        if (storageDto == null) {
            return;
        }
        Storage storage = batch.getStorageId() != null
                ? storageRepository.findById(batch.getStorageId()).orElse(new Storage())
                : new Storage();
        BeanUtils.copyProperties(storageDto, storage);
        storage.associateBatch(batch);
        storageRepository.save(storage);
        if (batch.getStorageId() == null) {
            batch.associateStorage(storage);
            batchRepository.save(batch);
        }
    }

    private void updateTransportSale(ProductionBatch batch, TransportSaleDTO transportSaleDto) {
        if (transportSaleDto == null) {
            return;
        }
        TransportSale transportSale = batch.getTransportSaleId() != null
                ? transportSaleRepository.findById(batch.getTransportSaleId()).orElse(new TransportSale())
                : new TransportSale();
        BeanUtils.copyProperties(transportSaleDto, transportSale);
        transportSale.associateBatch(batch);
        transportSaleRepository.save(transportSale);
        if (batch.getTransportSaleId() == null) {
            batch.associateTransportSale(transportSale);
            batchRepository.save(batch);
        }
    }

    @Override
    @Transactional
    public void deleteBatch(Long id) {
        ProductionBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));
        batch.softDelete();
        batchRepository.save(batch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionBatchDTO> listAllBatches() {
        return batchRepository.findByIsDeletedFalse().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionBatchDTO> getBatchesByProductId(Long productId) {
        return batchRepository.findByProductIdAndIsDeletedFalse(productId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionBatchDTO getBatchById(Long id) {
        ProductionBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));
        return toDTO(batch);
    }

    @Override
    public ProductionBatch getBatchByBatchNumber(String batchNumber) {
        return batchRepository.findByBatchNumberAndIsDeletedFalse(batchNumber)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));
    }

    private String generateBatchNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = nextBatchSeq();
        return "B" + dateStr + String.format("%04d", counter);
    }

    @Override
    @Transactional
    public ProductionBatch createQuickBatchForProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("产品不存在"));

        ProductionBatch batch = ProductionBatch.quickCreate(
                generateBatchNumber(), product.getId(), product.getShelfLife());
        return batchRepository.save(batch);
    }

    private ProductionBatchDTO toDTO(ProductionBatch batch) {
        ProductionBatchDTO dto = new ProductionBatchDTO();
        dto.setId(batch.getId());
        dto.setBatchNumber(batch.getBatchNumber());
        if (batch.getProduct() != null) {
            dto.setProductId(batch.getProduct().getId());
            dto.setProductName(batch.getProduct().getName());
        }
        dto.setProductionDate(batch.getProductionDate());
        dto.setShelfLife(batch.getShelfLife());
        dto.setQuantity(batch.getQuantity());
        dto.setUnit(batch.getUnit());
        dto.setCreatedAt(batch.getCreatedAt());
        return dto;
    }
}
