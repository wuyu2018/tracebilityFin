package com.foodtraceability.service;

import com.foodtraceability.anchor.entity.BlockchainAnchor;
import com.foodtraceability.anchor.repository.BlockchainAnchorRepository;
import com.foodtraceability.entity.BlockchainLog;
import com.foodtraceability.repository.BlockchainLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BlockchainAnchorService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainAnchorService.class);

    private final BlockchainLogRepository blockchainLogRepo;
    private final BlockchainAnchorRepository anchorRepo;
    private final BlockchainService blockchainService;

    public BlockchainAnchorService(BlockchainLogRepository blockchainLogRepo,
                                    BlockchainAnchorRepository anchorRepo,
                                    BlockchainService blockchainService) {
        this.blockchainLogRepo = blockchainLogRepo;
        this.anchorRepo = anchorRepo;
        this.blockchainService = blockchainService;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void dailyAnchor() {
        log.info("[BlockchainAnchor] Starting daily anchoring at {}", LocalDateTime.now());
        LocalDate today = LocalDate.now();
        List<String> anchorLines = new ArrayList<>();

        anchorChain("MATERIAL", null, today, anchorLines);

        List<Long> batchIds;
        try {
            batchIds = blockchainLogRepo.findByChainTypeOrderByTimestampAsc("BATCH")
                    .stream()
                    .map(BlockchainLog::getBatchId)
                    .distinct()
                    .toList();
        } catch (Exception e) {
            log.error("[BlockchainAnchor] Failed to query batch IDs, aborting BATCH anchoring", e);
            batchIds = List.of();
        }

        for (Long batchId : batchIds) {
            anchorChain("BATCH", batchId, today, anchorLines);
        }

        if (!anchorLines.isEmpty()) {
            writeAnchorLogFile(today, anchorLines);
        }
        log.info("[BlockchainAnchor] Daily anchoring completed: {} chains anchored", anchorLines.size());
    }

    private void anchorChain(String chainType, Long batchId, LocalDate today, List<String> lines) {
        Optional<BlockchainLog> latest = batchId != null
                ? blockchainLogRepo.findTopByChainTypeAndBatchIdOrderByTimestampDesc(chainType, batchId)
                : blockchainLogRepo.findTopByChainTypeOrderByTimestampDesc(chainType);

        if (latest.isEmpty()) {
            return;
        }

        String hash = latest.get().getCurrentHash();
        BlockchainAnchor anchor = BlockchainAnchor.create(chainType, batchId, hash, today);
        try {
            anchorRepo.save(anchor);
        } catch (Exception e) {
            log.error("[BlockchainAnchor] Failed to save anchor for chainType={}, batchId={}: {}",
                    chainType, batchId, e.getMessage());
            return;
        }

        String line = batchId != null
                ? String.format("%s | %s | %s | %s", chainType, batchId, hash, today)
                : String.format("%s | GLOBAL | %s | %s", chainType, hash, today);
        lines.add(line);
    }

    private void writeAnchorLogFile(LocalDate date, List<String> lines) {
        String dirPath = "logs";
        String filePath = dirPath + "/blockchain-anchor-" + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log";

        try {
            Files.createDirectories(Path.of(dirPath));
            try (FileWriter fw = new FileWriter(filePath, true)) {
                for (String line : lines) {
                    String signedLine = line + " | " + blockchainService.sign(line);
                    fw.write(signedLine + System.lineSeparator());
                }
            }
            log.info("[BlockchainAnchor] Anchor log written: {}", filePath);
        } catch (IOException e) {
            log.error("[BlockchainAnchor] Failed to write anchor log: {}", filePath, e);
        }
    }
}
