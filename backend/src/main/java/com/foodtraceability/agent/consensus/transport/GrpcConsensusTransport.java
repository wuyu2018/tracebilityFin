package com.foodtraceability.agent.consensus.transport;

import com.foodtraceability.agent.consensus.Endorsement;
import com.foodtraceability.agent.consensus.PbftMessage;
import com.foodtraceability.agent.consensus.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class GrpcConsensusTransport implements ConsensusTransport {

    private static final Logger log = LoggerFactory.getLogger(GrpcConsensusTransport.class);

    private final ConsensusPeerConfig peerConfig;
    private final Map<String, ConsensusServiceGrpc.ConsensusServiceBlockingStub> stubs;

    public GrpcConsensusTransport(ConsensusPeerConfig peerConfig,
                                   Object permissionContract,
                                   Object dataContract,
                                   Consumer<PbftMessage> prePrepareHandler,
                                   Consumer<PbftMessage> prepareHandler,
                                   Consumer<PbftMessage> commitHandler) {
        this.peerConfig = peerConfig;
        this.stubs = new ConcurrentHashMap<>();

        for (ConsensusPeerConfig.Peer peer : peerConfig.getPeers()) {
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(peer.getHost(), peer.getGrpcPort())
                    .usePlaintext()
                    .keepAliveTime(30, TimeUnit.SECONDS)
                    .keepAliveTimeout(10, TimeUnit.SECONDS)
                    .build();
            stubs.put(peer.getId(), ConsensusServiceGrpc.newBlockingStub(channel));
            log.info("gRPC channel established to peer: {} at {}", peer.getId(), peer.address());
        }
    }

    @Override
    public CompletableFuture<List<Endorsement>> endorseAll(String context, String digest, List<String> agentIds) {
        CompletableFuture<List<Endorsement>> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            List<Endorsement> results = new ArrayList<>();
            EndorsementRequestProto request = EndorsementRequestProto.newBuilder()
                    .setContext(context)
                    .setDigest(digest)
                    .setRequestingAgent(peerConfig.getAgentId())
                    .build();

            for (String agentId : agentIds) {
                try {
                    ConsensusServiceGrpc.ConsensusServiceBlockingStub stub = getStub(agentId);
                    EndorsementResponseProto response = stub.endorse(request);
                    results.add(PbftMessageConverter.fromEndorsementProto(response));
                } catch (Exception e) {
                    log.error("Endorsement failed for agent: {}", agentId, e);
                    results.add(new Endorsement(agentId, false, "", "Transport error: " + e.getMessage()));
                }
            }
            future.complete(results);
        });

        return future;
    }

    @Override
    public CompletableFuture<Boolean> broadcastPrePrepare(PbftMessage msg, List<String> targetIds) {
        return broadcastPbftMessage(msg, targetIds, (stub, proto) -> stub.sendPrePrepare(proto));
    }

    @Override
    public CompletableFuture<Boolean> broadcastPrepare(PbftMessage msg, List<String> targetIds) {
        return broadcastPbftMessage(msg, targetIds, (stub, proto) -> stub.sendPrepare(proto));
    }

    @Override
    public CompletableFuture<Boolean> broadcastCommit(PbftMessage msg, List<String> targetIds) {
        return broadcastPbftMessage(msg, targetIds, (stub, proto) -> stub.sendCommit(proto));
    }

    @Override
    public CompletableFuture<Void> notifyBlock(List<String> agentIds, String blockHash, String foodId, String chainType, Long blockHeaderId) {
        return CompletableFuture.runAsync(() -> {
            BlockNotificationProto notification = BlockNotificationProto.newBuilder()
                    .setBlockHash(blockHash)
                    .setFoodId(foodId)
                    .setChainType(chainType)
                    .setBlockHeaderId(blockHeaderId != null ? blockHeaderId : 0L)
                    .build();

            for (String agentId : agentIds) {
                try {
                    ConsensusServiceGrpc.ConsensusServiceBlockingStub stub = getStub(agentId);
                    stub.notifyBlock(notification);
                } catch (Exception e) {
                    log.warn("NotifyBlock failed for agent: {}", agentId, e);
                }
            }
        });
    }

    @Override
    public void registerHandler(PbftMessage.MessageType type, Consumer<PbftMessage> handler) {
    }

    private CompletableFuture<Boolean> broadcastPbftMessage(
            PbftMessage msg,
            List<String> targetIds,
            BiFunction<ConsensusServiceGrpc.ConsensusServiceBlockingStub, PbftMessageProto, AckResponseProto> rpc) {

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        PbftMessageProto proto = PbftMessageConverter.toProto(msg);

        CompletableFuture.runAsync(() -> {
            int successCount = 0;
            for (String targetId : targetIds) {
                try {
                    ConsensusServiceGrpc.ConsensusServiceBlockingStub stub = getStub(targetId);
                    AckResponseProto resp = rpc.apply(stub, proto);
                    if (resp.getSuccess()) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn("Broadcast {} to {} failed: {}", msg.getType(), targetId, e.getMessage());
                }
            }
            future.complete(successCount >= 3);
        });

        return future;
    }

    private ConsensusServiceGrpc.ConsensusServiceBlockingStub getStub(String agentId) {
        ConsensusServiceGrpc.ConsensusServiceBlockingStub stub = stubs.get(agentId);
        if (stub == null) {
            for (ConsensusPeerConfig.Peer peer : peerConfig.getPeers()) {
                if (peer.getId().equals(agentId)) {
                    ManagedChannel channel = ManagedChannelBuilder
                            .forAddress(peer.getHost(), peer.getGrpcPort())
                            .usePlaintext()
                            .keepAliveTime(30, TimeUnit.SECONDS)
                            .keepAliveTimeout(10, TimeUnit.SECONDS)
                            .build();
                    stub = ConsensusServiceGrpc.newBlockingStub(channel);
                    stubs.put(agentId, stub);
                    break;
                }
            }
        }
        if (stub == null) {
            throw new IllegalStateException("No gRPC stub found for agent: " + agentId);
        }
        return stub;
    }
}
