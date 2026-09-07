package com.foodtraceability.service;

import com.foodtraceability.anchor.repository.BlockchainAnchorRepository;
import com.foodtraceability.entity.BlockchainLog;
import com.foodtraceability.repository.BlockchainLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BlockchainMonitorService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainMonitorService.class);

    private final BlockchainLogRepository blockchainLogRepo;
    private final BlockchainAnchorRepository anchorRepo;
    private final PublicKey blockchainPublicKey;

    public BlockchainMonitorService(BlockchainLogRepository blockchainLogRepo,
                                    BlockchainAnchorRepository anchorRepo,
                                    KeyPair blockchainKeyPair) {
        this.blockchainLogRepo = blockchainLogRepo;
        this.anchorRepo = anchorRepo;
        this.blockchainPublicKey = blockchainKeyPair.getPublic();
    }

    public Map<String, Object> getSummary() {
        List<BlockchainLog> allLogs = blockchainLogRepo.findAll();

        List<BlockchainLog> materialLogs = allLogs.stream()
                .filter(l -> "MATERIAL".equals(l.getChainType()))
                .sorted(Comparator.comparing(BlockchainLog::getTimestamp))
                .toList();

        List<BlockchainLog> batchLogs = allLogs.stream()
                .filter(l -> "BATCH".equals(l.getChainType()))
                .sorted(Comparator.comparing(BlockchainLog::getTimestamp))
                .toList();

        Map<Long, List<BlockchainLog>> batchGroups = batchLogs.stream()
                .filter(l -> l.getBatchId() != null)
                .collect(Collectors.groupingBy(BlockchainLog::getBatchId));

        List<ChainCheckResult> materialResults = verifyChain(materialLogs);
        Map<Long, List<ChainCheckResult>> batchResults = new LinkedHashMap<>();
        for (Map.Entry<Long, List<BlockchainLog>> entry : batchGroups.entrySet()) {
            batchResults.put(entry.getKey(), verifyChain(entry.getValue()));
        }

        boolean materialIntact = materialResults.stream().allMatch(r -> r.errors.isEmpty());
        long intactBatchCount = batchResults.values().stream()
                .filter(list -> list.stream().allMatch(r -> r.errors.isEmpty()))
                .count();
        long totalBatches = batchGroups.size();
        long totalBatchBlocks = batchLogs.size();

        Optional<LocalDate> materialAnchor = anchorRepo.findLatestAnchorDateByChainType("MATERIAL");
        Optional<LocalDate> batchAnchor = anchorRepo.findLatestAnchorDateByChainType("BATCH");

        List<Map<String, Object>> brokenBlocks = new ArrayList<>();
        for (ChainCheckResult r : materialResults) {
            if (!r.errors.isEmpty()) {
                brokenBlocks.add(toBrokenBlockMap(r, null));
            }
        }
        for (Map.Entry<Long, List<ChainCheckResult>> entry : batchResults.entrySet()) {
            for (ChainCheckResult r : entry.getValue()) {
                if (!r.errors.isEmpty()) {
                    brokenBlocks.add(toBrokenBlockMap(r, entry.getKey()));
                }
            }
        }

        boolean overallHealthy = materialIntact && brokenBlocks.isEmpty();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("overallHealthy", overallHealthy);
        summary.put("lastUpdated", LocalDateTime.now().toString());

        Map<String, Object> materialChainMap = new LinkedHashMap<>();
        materialChainMap.put("intact", materialIntact);
        materialChainMap.put("blockCount", materialLogs.size());
        materialChainMap.put("lastAnchorDate", materialAnchor.map(LocalDate::toString).orElse(null));
        summary.put("materialChain", materialChainMap);

        Map<String, Object> batchChainsMap = new LinkedHashMap<>();
        batchChainsMap.put("totalBatches", totalBatches);
        batchChainsMap.put("intactCount", intactBatchCount);
        batchChainsMap.put("brokenCount", totalBatches - intactBatchCount);
        batchChainsMap.put("totalBlockCount", totalBatchBlocks);
        batchChainsMap.put("lastAnchorDate", batchAnchor.map(LocalDate::toString).orElse(null));
        summary.put("batchChains", batchChainsMap);

        summary.put("brokenBlocks", brokenBlocks);

        log.info("Blockchain monitor summary: healthy={}, materialBlocks={}, batchBlocks={}, brokenBlocks={}",
                overallHealthy, materialLogs.size(), totalBatchBlocks, brokenBlocks.size());

        return summary;
    }

    private List<ChainCheckResult> verifyChain(List<BlockchainLog> logs) {
        List<ChainCheckResult> results = new ArrayList<>();
        String expectedPreviousHash = null;

        for (int i = 0; i < logs.size(); i++) {
            BlockchainLog log = logs.get(i);
            ChainCheckResult r = new ChainCheckResult();
            r.blockId = log.getId();
            r.entityType = log.getEntityType();
            r.entityId = log.getEntityId();
            r.action = log.getAction();
            r.errors = new ArrayList<>();

            if (i > 0) {
                String prev = log.getPreviousHash();
                if (prev == null || !prev.equals(expectedPreviousHash)) {
                    r.errors.add("previous_hash mismatch");
                }
            } else {
                String prev = log.getPreviousHash();
                if (prev != null && !prev.isEmpty() && !"GENESIS".equals(prev)) {
                    r.errors.add("genesis block has unexpected previous_hash: " + prev);
                }
            }

            String recalculated = recalculateHash(log);
            if (recalculated != null && !recalculated.equals(log.getCurrentHash())) {
                r.errors.add("current_hash mismatch");
            }
            if (r.errors.isEmpty()) {
                String sig = log.getSignature();
                if (sig == null || sig.isEmpty()) {
                    r.errors.add("missing signature");
                } else if (!verifySignature(log.getCurrentHash(), sig)) {
                    r.errors.add("invalid signature");
                }
            }

            expectedPreviousHash = log.getCurrentHash();
            results.add(r);
        }

        return results;
    }

    private String recalculateHash(BlockchainLog entity) {
        try {
            String input = entity.getChainType() + "|" + entity.getEntityType() + "|"
                    + entity.getEntityId() + "|" + entity.getAction() + "|"
                    + (entity.getPreviousHash() != null ? entity.getPreviousHash() : "") + "|"
                    + (entity.getDataHash() != null ? entity.getDataHash() : "") + "|"
                    + entity.getTimestamp().truncatedTo(ChronoUnit.MILLIS);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            log.warn("Failed to recalculate hash for block {}: {}", entity.getId(), e.getMessage());
            return null;
        }
    }

    private boolean verifySignature(String hash, String signatureBase64) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(blockchainPublicKey);
            sig.update(hash.getBytes(StandardCharsets.UTF_8));
            return sig.verify(java.util.Base64.getDecoder().decode(signatureBase64));
        } catch (Exception e) {
            log.warn("Failed to verify signature: {}", e.getMessage());
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private Map<String, Object> toBrokenBlockMap(ChainCheckResult r, Long batchId) {
        Map<String, Object> bb = new LinkedHashMap<>();
        bb.put("blockId", r.blockId);
        bb.put("batchId", batchId);
        bb.put("action", r.action);
        bb.put("entityType", r.entityType);
        bb.put("entityId", r.entityId);
        bb.put("errors", r.errors);
        return bb;
    }

    private static class ChainCheckResult {
        Long blockId;
        String entityType;
        Long entityId;
        String action;
        List<String> errors;
    }
}
