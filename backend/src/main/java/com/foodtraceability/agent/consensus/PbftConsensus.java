package com.foodtraceability.agent.consensus;

import com.foodtraceability.agent.consensus.grpc.ConsensusPeerConfig;
import com.foodtraceability.agent.consensus.transport.ConsensusTransport;
import com.foodtraceability.agent.contract.DataOnChainContract;
import com.foodtraceability.agent.contract.PermissionControlContract;
import com.foodtraceability.agent.core.Agent;
import com.foodtraceability.agent.impl.CertificateAuthorityAgent;
import com.foodtraceability.entity.ConsensusRecord;
import com.foodtraceability.repository.ConsensusRecordRepository;
import com.foodtraceability.service.BlockchainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class PbftConsensus {

    private static final Logger log = LoggerFactory.getLogger(PbftConsensus.class);

    private final AtomicLong sequenceNumber;
    private final String view;
    private String primaryAgentId;
    private List<String> replicaNodeIds;

    private final Map<Long, ConsensusState> consensusStates;
    private final Map<Long, List<Endorsement>> endorsementPool;

    public static final int MAX_FAULTY_NODES = 1;
    private static final Duration PHASE_TIMEOUT = Duration.ofSeconds(30);

    private final ConsensusTransport transport;
    private final ConsensusPeerConfig peerConfig;
    private final ConsensusRecordRepository consensusRecordRepo;
    private final BlockchainService blockchainService;

    public PbftConsensus(ConsensusTransport transport, ConsensusPeerConfig peerConfig,
                          ConsensusRecordRepository consensusRecordRepo,
                          BlockchainService blockchainService) {
        this.sequenceNumber = new AtomicLong(0);
        this.view = "0";
        this.consensusStates = new ConcurrentHashMap<>();
        this.endorsementPool = new ConcurrentHashMap<>();
        this.replicaNodeIds = new ArrayList<>();
        this.transport = transport;
        this.peerConfig = peerConfig;
        this.consensusRecordRepo = consensusRecordRepo;
        this.blockchainService = blockchainService;
    }

    public void initialize(String caAgentId, List<String> replicaIds) {
        this.primaryAgentId = caAgentId;
        this.replicaNodeIds.clear();
        this.replicaNodeIds.addAll(replicaIds);
        log.info("PBFT consensus initialized. Primary (CA-Agent): {}, Replicas: {}",
                primaryAgentId, replicaNodeIds);
    }

    public boolean isPrimary(String agentId) {
        return primaryAgentId != null && primaryAgentId.equals(agentId);
    }

    public String getPrimaryAgentId() {
        return primaryAgentId;
    }

    public long getNextSequenceNumber() {
        return sequenceNumber.incrementAndGet();
    }

    public List<String> getReplicaNodeIds() {
        return Collections.unmodifiableList(replicaNodeIds);
    }

    // ========== Transport-aware Consensus Methods (gRPC mode) ==========

    public long createAndPersistRequest(String digest) {
        long seqNum = sequenceNumber.incrementAndGet();
        getOrCreateState(seqNum).startedAt = Instant.now();

        ConsensusRecord record = new ConsensusRecord();
        record.setSequenceNumber(seqNum);
        record.setView(view);
        record.setDigest(digest);
        record.setPhase(ConsensusRecord.ConsensusPhase.PRE_PREPARE);
        record.setStatus(ConsensusRecord.ConsensusStatus.PENDING);
        consensusRecordRepo.save(record);

        log.info("Consensus request created: seq={}, digest={}", seqNum, digest);
        return seqNum;
    }

    public PbftMessage createPrePrepare(long seqNum, String digest) {
        PbftMessage prePrepare = new PbftMessage(
                PbftMessage.MessageType.PRE_PREPARE,
                view,
                seqNum,
                digest,
                primaryAgentId
        );

        ConsensusState state = getOrCreateState(seqNum);
        state.addPrePrepare(prePrepare);
        state.setPrePrepareAccepted(true);

        updateConsensusRecord(seqNum, ConsensusRecord.ConsensusPhase.PRE_PREPARE, ConsensusRecord.ConsensusStatus.ACCEPTED);

        log.debug("Created PRE-PREPARE message: seq={}", seqNum);
        return prePrepare;
    }

    public void broadcastPrePrepareViaTransport(PbftMessage prePrepare) {
        List<String> peerIds = new ArrayList<>(replicaNodeIds);
        peerIds.remove(peerConfig.getAgentId());

        CompletableFuture.runAsync(() -> {
            transport.broadcastPrePrepare(prePrepare, peerIds).thenAccept(success -> {
                if (success) {
                    updateConsensusRecord(prePrepare.getSequenceNumber(),
                            ConsensusRecord.ConsensusPhase.PREPARE, ConsensusRecord.ConsensusStatus.PENDING);
                }
            });
        });
    }

    public void handleIncomingPrePrepare(PbftMessage msg) {
        long seqNum = msg.getSequenceNumber();
        log.info("Handling incoming PRE-PREPARE: seq={}, from={}", seqNum, msg.getSenderId());

        ConsensusState state = getOrCreateState(seqNum);
        state.startedAt = Instant.now();
        state.addPrePrepare(msg);

        if (validatePrePrepare(msg)) {
            state.setPrePrepareAccepted(true);
            updateConsensusRecord(seqNum, ConsensusRecord.ConsensusPhase.PRE_PREPARE, ConsensusRecord.ConsensusStatus.ACCEPTED);

            List<String> peerIds = new ArrayList<>(replicaNodeIds);
            peerIds.remove(peerConfig.getAgentId());
            PbftMessage prepare = new PbftMessage(
                    PbftMessage.MessageType.PREPARE, view,
                    seqNum, msg.getDigest(), peerConfig.getAgentId());
            state.addPrepare(prepare);

            CompletableFuture.runAsync(() -> {
                transport.broadcastPrepare(prepare, peerIds);
            });
        } else {
            updateConsensusRecord(seqNum, ConsensusRecord.ConsensusPhase.PRE_PREPARE, ConsensusRecord.ConsensusStatus.REJECTED);
        }
    }

    public void handleIncomingPrepare(PbftMessage msg) {
        long seqNum = msg.getSequenceNumber();
        log.debug("Handling incoming PREPARE: seq={}, from={}", seqNum, msg.getSenderId());

        ConsensusState state = getOrCreateState(seqNum);
        state.addPrepare(msg);

        int prepareCount = state.getPrepareCount();
        updateConsensusRecordPrepare(seqNum, prepareCount);

        if (state.isPrepared() && !state.isCommitBroadcasted()) {
            state.setCommitBroadcasted(true);
            List<String> peerIds = new ArrayList<>(replicaNodeIds);
            peerIds.remove(peerConfig.getAgentId());

            PbftMessage commit = new PbftMessage(
                    PbftMessage.MessageType.COMMIT, view,
                    seqNum, msg.getDigest(), peerConfig.getAgentId());
            state.addCommit(commit);

            CompletableFuture.runAsync(() -> {
                transport.broadcastCommit(commit, peerIds);
                updateConsensusRecord(seqNum, ConsensusRecord.ConsensusPhase.COMMIT, ConsensusRecord.ConsensusStatus.PENDING);
            });
        }
    }

    public void handleIncomingCommit(PbftMessage msg) {
        long seqNum = msg.getSequenceNumber();
        log.debug("Handling incoming COMMIT: seq={}, from={}", seqNum, msg.getSenderId());

        ConsensusState state = getOrCreateState(seqNum);
        state.addCommit(msg);

        int commitCount = state.getCommitCount();
        updateConsensusRecordCommit(seqNum, commitCount);

        if (canExecute(seqNum)) {
            markExecuted(seqNum);
            log.info("Consensus completed for seq: {}", seqNum);
        }
    }

    public void handleBlockNotification(String blockHash, String foodId, String chainType, Long blockHeaderId) {
        log.info("Block notification received: hash={}, foodId={}, chainType={}",
                blockHash, foodId, chainType);
    }

    public Long getExecutedSequenceNumber() {
        return sequenceNumber.get();
    }

    // ========== Consensus State Persistence ==========

    private void updateConsensusRecord(long seqNum, ConsensusRecord.ConsensusPhase phase, ConsensusRecord.ConsensusStatus status) {
        consensusRecordRepo.findBySequenceNumber(seqNum).ifPresentOrElse(record -> {
            record.setPhase(phase);
            record.setStatus(status);
            consensusRecordRepo.save(record);
        }, () -> {
            ConsensusRecord record = new ConsensusRecord();
            record.setSequenceNumber(seqNum);
            record.setView(view);
            record.setPhase(phase);
            record.setStatus(status);
            consensusRecordRepo.save(record);
        });
    }

    private void updateConsensusRecordPrepare(long seqNum, int prepareCount) {
        consensusRecordRepo.findBySequenceNumber(seqNum).ifPresent(record -> {
            record.setPrepareCount(prepareCount);
            consensusRecordRepo.save(record);
        });
    }

    private void updateConsensusRecordCommit(long seqNum, int commitCount) {
        consensusRecordRepo.findBySequenceNumber(seqNum).ifPresent(record -> {
            record.setCommitCount(commitCount);
            consensusRecordRepo.save(record);
        });
    }

    // ========== Original Endorsement Phase ==========

    public boolean requestEndorsement(String context, List<Agent> agents,
                                       PermissionControlContract permissionContract,
                                       DataOnChainContract dataContract,
                                       CertificateAuthorityAgent caAgent) {
        long seqNum = sequenceNumber.get() + 1;
        String digest = calculateDigest(context);

        List<Endorsement> results = new ArrayList<>();

        for (Agent agent : agents) {
            boolean approved = true;
            String reason = "";

            if (!agent.isAuthorized()) {
                approved = false;
                reason = "Agent not authorized";
            }

            if (approved) {
                String permContext = agent.getAgentId() + "|WRITE";
                if (!permissionContract.validate(permContext)) {
                    approved = false;
                    reason = "Permission denied";
                }
            }

            if (approved && !dataContract.validate(context)) {
                approved = false;
                reason = "Data validation failed";
            }

            String signature = signMessage(digest, agent.getAgentId());
            results.add(new Endorsement(agent.getAgentId(), approved, signature, reason));

            log.debug("Endorsement from {}: approved={}, reason={}",
                    agent.getAgentId(), approved, reason.isEmpty() ? "ok" : reason);
        }

        endorsementPool.put(seqNum, results);
        return verifyEndorsements(seqNum);
    }

    public boolean verifyEndorsements(Long sequenceNumber) {
        List<Endorsement> results = endorsementPool.get(sequenceNumber);
        if (results == null || results.isEmpty()) return false;

        long approvedCount = results.stream().filter(Endorsement::approved).count();
        boolean majorityApproved = approvedCount >= 2 * MAX_FAULTY_NODES + 1;

        if (!majorityApproved) {
            log.warn("Endorsement rejected: only {}/{} approved", approvedCount, results.size());
            return false;
        }

        log.info("Endorsement passed: {}/{} approved", approvedCount, results.size());
        return true;
    }

    // ========== Original PBFT Message Flow ==========

    public PbftMessage createRequest(String clientRequest) {
        long seqNum = sequenceNumber.incrementAndGet();
        String digest = calculateDigest(clientRequest);

        PbftMessage request = new PbftMessage(
            PbftMessage.MessageType.REQUEST,
            view,
            seqNum,
            digest,
            primaryAgentId
        );

        log.debug("Created REQUEST message: seq={}, digest={}", seqNum, digest);
        return request;
    }

    public PbftMessage createPrePrepare(PbftMessage request, String requestingAgentId) {
        if (!isPrimary(requestingAgentId)) {
            throw new IllegalStateException(
                "Only primary (CA-Agent) can create PRE-PREPARE message, but caller is " + requestingAgentId);
        }

        PbftMessage prePrepare = new PbftMessage(
            PbftMessage.MessageType.PRE_PREPARE,
            view,
            request.getSequenceNumber(),
            request.getDigest(),
            primaryAgentId
        );

        log.debug("Created PRE-PREPARE message: seq={}", request.getSequenceNumber());
        return prePrepare;
    }

    public PbftMessage createPrepare(PbftMessage prePrepare) {
        PbftMessage prepare = new PbftMessage(
            PbftMessage.MessageType.PREPARE,
            view,
            prePrepare.getSequenceNumber(),
            prePrepare.getDigest(),
            primaryAgentId
        );

        log.debug("Created PREPARE message: seq={}", prePrepare.getSequenceNumber());
        return prepare;
    }

    public PbftMessage createCommit(PbftMessage prepare) {
        PbftMessage commit = new PbftMessage(
            PbftMessage.MessageType.COMMIT,
            view,
            prepare.getSequenceNumber(),
            prepare.getDigest(),
            primaryAgentId
        );

        log.debug("Created COMMIT message: seq={}", prepare.getSequenceNumber());
        return commit;
    }

    public ConsensusResult receivePrePrepare(PbftMessage prePrepare) {
        ConsensusState state = getOrCreateState(prePrepare.getSequenceNumber());
        state.addPrePrepare(prePrepare);

        if (validatePrePrepare(prePrepare)) {
            state.setPrePrepareAccepted(true);
            return ConsensusResult.ACCEPTED;
        } else {
            return ConsensusResult.REJECTED;
        }
    }

    public int receivePrepare(PbftMessage prepare) {
        ConsensusState state = getOrCreateState(prepare.getSequenceNumber());
        state.addPrepare(prepare);

        int prepareCount = state.getPrepareCount();
        log.debug("Received PREPARE from {}. Total prepares: {}",
                 prepare.getSenderId(), prepareCount);

        if (prepareCount >= 2 * MAX_FAULTY_NODES + 1) {
            return prepareCount;
        }
        return prepareCount;
    }

    public int receiveCommit(PbftMessage commit) {
        ConsensusState state = getOrCreateState(commit.getSequenceNumber());
        state.addCommit(commit);

        int commitCount = state.getCommitCount();
        log.debug("Received COMMIT from {}. Total commits: {}",
                 commit.getSenderId(), commitCount);

        return commitCount;
    }

    public boolean canExecute(Long sequenceNumber) {
        ConsensusState state = consensusStates.get(sequenceNumber);
        if (state == null) {
            return false;
        }

        int faultyNodes = MAX_FAULTY_NODES;
        int requiredCommits = 2 * faultyNodes + 1;

        return state.isPrepared() && state.getCommitCount() >= requiredCommits;
    }

    // ========== Full Consensus (In-Process / Dev mode) ==========

    public boolean runFullConsensus(String context, List<Agent> allAgents,
                                     PermissionControlContract permissionContract,
                                     DataOnChainContract dataContract,
                                     CertificateAuthorityAgent caAgent,
                                     String requestingAgentId) {
        if (!requestEndorsement(context, allAgents, permissionContract, dataContract, caAgent)) {
            log.warn("Consensus failed at endorsement phase");
            return false;
        }

        PbftMessage request = createRequest(context);

        if (!isPrimary(requestingAgentId)) {
            log.warn("Non-primary agent {} cannot initiate PBFT", requestingAgentId);
            return false;
        }

        PbftMessage prePrepare = createPrePrepare(request, requestingAgentId);

        ConsensusResult accepted = receivePrePrepare(prePrepare);
        if (accepted != ConsensusResult.ACCEPTED) {
            log.warn("PRE-PREPARE rejected");
            return false;
        }

        for (String replicaId : replicaNodeIds) {
            if (!isPrimary(replicaId)) {
                PbftMessage prepare = new PbftMessage(
                    PbftMessage.MessageType.PREPARE, view,
                    prePrepare.getSequenceNumber(), prePrepare.getDigest(), replicaId);
                receivePrepare(prepare);
            }
        }

        PbftMessage primaryPrepare = createPrepare(prePrepare);
        receivePrepare(primaryPrepare);

        if (!consensusStates.containsKey(request.getSequenceNumber())
                || !consensusStates.get(request.getSequenceNumber()).isPrepared()) {
            log.warn("PBFT: not enough prepares, consensus failed");
            return false;
        }

        for (String replicaId : replicaNodeIds) {
            PbftMessage commit = new PbftMessage(
                PbftMessage.MessageType.COMMIT, view,
                prePrepare.getSequenceNumber(), prePrepare.getDigest(), replicaId);
            receiveCommit(commit);
        }

        if (!canExecute(request.getSequenceNumber())) {
            log.warn("PBFT: not enough commits, consensus failed");
            return false;
        }

        markExecuted(request.getSequenceNumber());
        log.info("PBFT consensus reached for seq: {}", request.getSequenceNumber());
        return true;
    }

    public void markExecuted(Long sequenceNumber) {
        ConsensusState state = consensusStates.get(sequenceNumber);
        if (state != null) {
            state.setExecuted(true);
            endorsementPool.remove(sequenceNumber);
            consensusRecordRepo.findBySequenceNumber(sequenceNumber).ifPresent(record -> {
                record.setPhase(ConsensusRecord.ConsensusPhase.EXECUTED);
                record.setStatus(ConsensusRecord.ConsensusStatus.COMMITTED);
                consensusRecordRepo.save(record);
            });
            log.info("Consensus reached for sequence: {}", sequenceNumber);
        }
    }

    private ConsensusState getOrCreateState(Long sequenceNumber) {
        return consensusStates.computeIfAbsent(sequenceNumber, k -> new ConsensusState());
    }

    private boolean validatePrePrepare(PbftMessage prePrepare) {
        return prePrepare.getType() == PbftMessage.MessageType.PRE_PREPARE &&
               prePrepare.getView().equals(view) &&
               prePrepare.getDigest() != null;
    }

    private String calculateDigest(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    String signMessage(String digest, String agentId) {
        return blockchainService.sign(digest);
    }

    public enum ConsensusResult {
        ACCEPTED,
        REJECTED,
        PENDING
    }

    static class ConsensusState {
        private PbftMessage prePrepare;
        private final Map<String, PbftMessage> prepares;
        private final Map<String, PbftMessage> commits;
        private volatile boolean prePrepareAccepted;
        private volatile boolean executed;
        private volatile boolean commitBroadcasted;
        private volatile Instant startedAt;

        private ConsensusState() {
            this.prepares = new ConcurrentHashMap<>();
            this.commits = new ConcurrentHashMap<>();
            this.prePrepareAccepted = false;
            this.executed = false;
            this.commitBroadcasted = false;
            this.startedAt = Instant.now();
        }

        public void addPrePrepare(PbftMessage message) { this.prePrepare = message; }
        public void addPrepare(PbftMessage message) { this.prepares.put(message.getSenderId(), message); }
        public void addCommit(PbftMessage message) { this.commits.put(message.getSenderId(), message); }
        public int getPrepareCount() { return prepares.size() + (prePrepare != null ? 1 : 0); }
        public int getCommitCount() { return commits.size(); }
        public boolean isPrepared() { return prePrepareAccepted && getPrepareCount() >= 2 * MAX_FAULTY_NODES + 1; }
        public boolean isPrePrepareAccepted() { return prePrepareAccepted; }
        public void setPrePrepareAccepted(boolean accepted) { this.prePrepareAccepted = accepted; }
        public boolean isExecuted() { return executed; }
        public void setExecuted(boolean executed) { this.executed = executed; }
        public boolean isCommitBroadcasted() { return commitBroadcasted; }
        public void setCommitBroadcasted(boolean broadcasted) { this.commitBroadcasted = broadcasted; }
        public boolean isExpired() {
            return Duration.between(startedAt, Instant.now()).compareTo(PHASE_TIMEOUT) > 0;
        }
    }
}
