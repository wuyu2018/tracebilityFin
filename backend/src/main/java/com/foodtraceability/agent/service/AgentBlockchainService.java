package com.foodtraceability.agent.service;

import com.foodtraceability.agent.consensus.Endorsement;
import com.foodtraceability.agent.consensus.PbftConsensus;
import com.foodtraceability.agent.consensus.grpc.ConsensusPeerConfig;
import com.foodtraceability.agent.consensus.transport.ConsensusTransport;
import com.foodtraceability.agent.contract.DataOnChainContract;
import com.foodtraceability.agent.contract.PermissionControlContract;
import com.foodtraceability.agent.core.Agent;
import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.foodtraceability.entity.BlockHeader;
import com.foodtraceability.entity.BlockchainLog;
import com.foodtraceability.entity.OffchainStorage;
import com.foodtraceability.repository.BlockHeaderRepository;
import com.foodtraceability.repository.BlockchainLogRepository;
import com.foodtraceability.repository.OffchainStorageRepository;
import com.foodtraceability.security.AgentKeyManager;
import com.foodtraceability.security.BloomFilterManager;
import com.foodtraceability.security.MerkleTree;
import com.foodtraceability.service.BlockchainService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AgentBlockchainService {
    @PersistenceContext
    private EntityManager em;

    private static final Logger log = LoggerFactory.getLogger(AgentBlockchainService.class);
    private static final Duration CONSENSUS_WAIT_TIMEOUT = Duration.ofSeconds(60);

    private final MultiAgentCoordinator agentCoordinator;
    private final DataOnChainContract dataOnChainContract;
    private final PermissionControlContract permissionControlContract;
    private final OffchainStorageRepository offchainStorageRepo;
    private final AgentKeyManager keyManager;
    private final BloomFilterManager bloomFilterManager;
    private final BlockHeaderRepository blockHeaderRepo;
    private final BlockchainLogRepository blockchainLogRepo;
    private final PbftConsensus pbftConsensus;
    private final ConsensusTransport consensusTransport;
    private final ConsensusPeerConfig peerConfig;
    private final BlockchainService blockchainService;

    public AgentBlockchainService(
            MultiAgentCoordinator agentCoordinator,
            DataOnChainContract dataOnChainContract,
            PermissionControlContract permissionControlContract,
            OffchainStorageRepository offchainStorageRepo,
            AgentKeyManager keyManager,
            BloomFilterManager bloomFilterManager,
            BlockHeaderRepository blockHeaderRepo,
            BlockchainLogRepository blockchainLogRepo,
            PbftConsensus pbftConsensus,
            ConsensusTransport consensusTransport,
            ConsensusPeerConfig peerConfig,
            BlockchainService blockchainService) {
        this.agentCoordinator = agentCoordinator;
        this.dataOnChainContract = dataOnChainContract;
        this.permissionControlContract = permissionControlContract;
        this.offchainStorageRepo = offchainStorageRepo;
        this.keyManager = keyManager;
        this.bloomFilterManager = bloomFilterManager;
        this.blockHeaderRepo = blockHeaderRepo;
        this.blockchainLogRepo = blockchainLogRepo;
        this.pbftConsensus = pbftConsensus;
        this.consensusTransport = consensusTransport;
        this.peerConfig = peerConfig;
        this.blockchainService = blockchainService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BlockchainLog appendBlockWithConsensus(
            String chainType,
            String entityType,
            Long entityId,
            String action,
            String rawData,
            Long operatorId) {

        log.info("Appending block with consensus: type={}, entity={}, id={}",
                chainType, entityType, entityId);

        var currentAgent = getCurrentAgentForChainType(chainType);

        if (!currentAgent.isAuthorized()) {
            throw new IllegalStateException("Agent not authorized for blockchain operation");
        }

        String dataHash = calculateDataHash(rawData);
        String context = entityType + "|" + entityId + "|" + action + "|" + dataHash;

        if (!dataOnChainContract.validate(context)) {
            throw new IllegalStateException("Data on-chain validation failed");
        }

        if (peerConfig.isGrpcEnabled()) {
            return appendBlockWithGrpcConsensus(chainType, entityType, entityId, action,
                    context, dataHash, rawData, operatorId, currentAgent);
        }

        boolean consensusReached = pbftConsensus.runFullConsensus(
                context,
                agentCoordinator.getAllAgents().stream().toList(),
                permissionControlContract,
                dataOnChainContract,
                agentCoordinator.getCaAgent(),
                agentCoordinator.getCaAgent().getAgentId());

        if (!consensusReached) {
            throw new IllegalStateException("Consensus not reached for block append");
        }

        return saveBlockWithEncryption(chainType, entityType, entityId, action,
                rawData, dataHash, operatorId, currentAgent);
    }

    private BlockchainLog appendBlockWithGrpcConsensus(
            String chainType, String entityType, Long entityId, String action,
            String context, String dataHash, String rawData, Long operatorId, Agent currentAgent) {

        List<String> agentIds = peerConfig.getPeers().stream()
                .map(ConsensusPeerConfig.Peer::getId)
                .toList();

        List<Endorsement> endorsements;
        try {
            endorsements = consensusTransport.endorseAll(context, dataHash, agentIds)
                    .get(CONSENSUS_WAIT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Endorsement failed", e);
            throw new IllegalStateException("Endorsement failed: " + e.getMessage());
        }

        long approvedCount = endorsements.stream().filter(Endorsement::approved).count();
        if (approvedCount < 2 * PbftConsensus.MAX_FAULTY_NODES + 1) {
            log.warn("Endorsement rejected: only {}/{} approved", approvedCount, endorsements.size());
            throw new IllegalStateException("Not enough endorsements for consensus: " + approvedCount + "/" + endorsements.size());
        }

        log.info("Endorsement passed: {}/{} approved", approvedCount, endorsements.size());

        return saveBlockWithEncryption(chainType, entityType, entityId, action,
                rawData, dataHash, operatorId, currentAgent);
    }

    private BlockchainLog saveBlockWithEncryption(
            String chainType, String entityType, Long entityId, String action,
            String rawData, String dataHash, Long operatorId, Agent currentAgent) {

        String foodId = generateFoodId(entityType, entityId);
        bloomFilterManager.add(chainType, foodId);

        String encryptedData;
        String aesKey;
        try {
            aesKey = keyManager.generateAesKey();
            encryptedData = keyManager.encryptData(rawData, aesKey);
        } catch (Exception e) {
            log.error("Failed to encrypt data", e);
            throw new RuntimeException("Data encryption failed", e);
        }

        String encryptedAesKey;
        try {
            encryptedAesKey = keyManager.encryptAesKeyForAgent(aesKey, currentAgent.getAgentId());
        } catch (Exception e) {
            log.error("Failed to encrypt AES key", e);
            throw new RuntimeException("AES key encryption failed", e);
        }

        OffchainStorage offchainStorage = new OffchainStorage();
        offchainStorage.setFoodId(foodId);
        offchainStorage.setDataHash(dataHash);
        offchainStorage.setStorageType(OffchainStorage.StorageType.DATABASE);
        offchainStorage.setStorageKey("blockchain_log:" + entityId);
        offchainStorage.setEncryptionMethod("AES-256-GCM");
        offchainStorage.setEncryptedData(encryptedData);
        offchainStorage.setEncryptedAesKey(encryptedAesKey);
        offchainStorage.setOwnerAgentId(Long.parseLong(currentAgent.getAgentId().split("-")[1]));
        offchainStorageRepo.save(offchainStorage);

        String previousHash = getPreviousBlockHash(chainType,
                "MATERIAL".equals(chainType) ? null : entityId);
        String merkleRoot = MerkleTree.computeRoot(List.of(dataHash));
        String metadataIndex = String.format(
                "{\"entityType\":\"%s\",\"entityId\":%d,\"action\":\"%s\"}", entityType, entityId, action);
        LocalDateTime now = LocalDateTime.now();

        BlockHeader header = BlockHeader.create(chainType, previousHash, merkleRoot,
                bloomFilterManager.toBytes(chainType), metadataIndex, 1, now);
        blockHeaderRepo.save(header);

        String currentHash = calculateBlockHash(
            chainType, entityType, entityId, action, previousHash, dataHash, now);
        String signature = signHash(currentHash);
        String refMasterChainHash = getLatestMasterChainHash(chainType);

        BlockchainLog block = BlockchainLog.createOptimizedBlock(
            chainType,
            "MATERIAL".equals(chainType) ? null : entityId,
            entityType,
            entityId,
            action,
            previousHash,
            currentHash,
            signature,
            now,
            operatorId,
            dataHash,
            offchainStorage.getFoodId(),
            refMasterChainHash
        );
        block.setBlockHeader(header);
        block = blockchainLogRepo.save(block);

        currentAgent.updateCreditScore(1);

        if (peerConfig.isGrpcEnabled()) {
            List<String> agentIds = peerConfig.getPeers().stream()
                    .map(ConsensusPeerConfig.Peer::getId)
                    .filter(id -> !id.equals(peerConfig.getAgentId()))
                    .toList();
            consensusTransport.notifyBlock(agentIds, header.getBlockHash(), foodId,
                    chainType, header.getId());
        }
        em.flush();  // 强制发送所有 INSERT 到数据库
        log.info("Block appended successfully: blockHash={}, logHash={}, foodId={}",
                header.getBlockHash(), currentHash, foodId);

        return block;
    }

    private String getPreviousBlockHash(String chainType, Long batchId) {
        if ("BATCH".equals(chainType) && batchId != null) {
            return blockchainLogRepo.findTopByChainTypeAndBatchIdOrderByTimestampDesc(chainType, batchId)
                    .map(BlockchainLog::getCurrentHash)
                    .orElse("GENESIS");
        }
        return blockchainLogRepo.findTopByChainTypeOrderByTimestampDesc(chainType)
                .map(BlockchainLog::getCurrentHash)
                .orElse("GENESIS");
    }

    private String getLatestMasterChainHash(String chainType) {
        String oppositeChain = "MATERIAL".equals(chainType) ? "BATCH" : "MATERIAL";
        return blockchainLogRepo.findTopByChainTypeOrderByTimestampDesc(oppositeChain)
                .map(BlockchainLog::getCurrentHash)
                .orElse(null);
    }

    private String calculateBlockHash(String chainType, String entityType, Long entityId,
                                      String action, String previousHash, String dataHash,
                                      LocalDateTime timestamp) {
String input = chainType + "|" + entityType + "|" + entityId + "|" + action +
              "|" + previousHash + "|" + dataHash + "|" + timestamp.truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hash", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String calculateDataHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String generateFoodId(String entityType, Long entityId) {
        return "FOOD-" + entityType + "-" + entityId + "-" + System.currentTimeMillis();
    }

    private String signHash(String hash) {
        return blockchainService.sign(hash);
    }

    private Agent getCurrentAgentForChainType(String chainType) {
        switch (chainType) {
            case "MATERIAL":
                return agentCoordinator.getProductionAgent();
            case "BATCH":
                return agentCoordinator.getProductionAgent();
            case "TRANSPORT":
                return agentCoordinator.getCirculationAgent();
            case "SALES":
                return agentCoordinator.getSalesAgent();
            default:
                return agentCoordinator.getProductionAgent();
        }
    }
}
