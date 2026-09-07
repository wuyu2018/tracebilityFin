package com.foodtraceability.agent.consensus.transport;

import com.foodtraceability.agent.consensus.Endorsement;
import com.foodtraceability.agent.consensus.PbftMessage;
import com.foodtraceability.agent.core.Agent;
import com.foodtraceability.agent.contract.DataOnChainContract;
import com.foodtraceability.agent.contract.PermissionControlContract;
import com.foodtraceability.service.BlockchainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InProcessConsensusTransport implements ConsensusTransport {

    private static final Logger log = LoggerFactory.getLogger(InProcessConsensusTransport.class);

    private final Supplier<List<Agent>> agentsSupplier;
    private final PermissionControlContract permissionContract;
    private final DataOnChainContract dataContract;
    private final BlockchainService blockchainService;
    private final Map<PbftMessage.MessageType, Consumer<PbftMessage>> handlers;

    public InProcessConsensusTransport(Supplier<List<Agent>> agentsSupplier,
                                        PermissionControlContract permissionContract,
                                        DataOnChainContract dataContract,
                                        BlockchainService blockchainService) {
        this.agentsSupplier = agentsSupplier;
        this.permissionContract = permissionContract;
        this.dataContract = dataContract;
        this.blockchainService = blockchainService;
        this.handlers = new EnumMap<>(PbftMessage.MessageType.class);
    }

    @Override
    public CompletableFuture<List<Endorsement>> endorseAll(String context, String digest, List<String> agentIds) {
        return CompletableFuture.supplyAsync(() -> {
            List<Endorsement> results = new ArrayList<>();
            for (Agent agent : agentsSupplier.get()) {
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

                String signature = blockchainService.sign(digest);
                results.add(new Endorsement(agent.getAgentId(), approved, signature, reason));
            }
            return results;
        });
    }

    @Override
    public CompletableFuture<Boolean> broadcastPrePrepare(PbftMessage msg, List<String> targetIds) {
        return CompletableFuture.supplyAsync(() -> {
            Consumer<PbftMessage> handler = handlers.get(PbftMessage.MessageType.PRE_PREPARE);
            if (handler != null) {
                handler.accept(msg);
            }
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> broadcastPrepare(PbftMessage msg, List<String> targetIds) {
        return CompletableFuture.supplyAsync(() -> {
            Consumer<PbftMessage> handler = handlers.get(PbftMessage.MessageType.PREPARE);
            if (handler != null) {
                handler.accept(msg);
            }
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> broadcastCommit(PbftMessage msg, List<String> targetIds) {
        return CompletableFuture.supplyAsync(() -> {
            Consumer<PbftMessage> handler = handlers.get(PbftMessage.MessageType.COMMIT);
            if (handler != null) {
                handler.accept(msg);
            }
            return true;
        });
    }

    @Override
    public CompletableFuture<Void> notifyBlock(List<String> agentIds, String blockHash, String foodId, String chainType, Long blockHeaderId) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void registerHandler(PbftMessage.MessageType type, Consumer<PbftMessage> handler) {
        handlers.put(type, handler);
    }
}
