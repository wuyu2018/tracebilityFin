package com.foodtraceability.agent.consensus.transport;

import com.foodtraceability.agent.consensus.PbftMessage;
import com.foodtraceability.agent.consensus.Endorsement;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ConsensusTransport {

    CompletableFuture<List<Endorsement>> endorseAll(String context, String digest, List<String> agentIds);

    CompletableFuture<Boolean> broadcastPrePrepare(PbftMessage msg, List<String> targetIds);

    CompletableFuture<Boolean> broadcastPrepare(PbftMessage msg, List<String> targetIds);

    CompletableFuture<Boolean> broadcastCommit(PbftMessage msg, List<String> targetIds);

    CompletableFuture<Void> notifyBlock(List<String> agentIds, String blockHash, String foodId, String chainType, Long blockHeaderId);

    void registerHandler(PbftMessage.MessageType type, Consumer<PbftMessage> handler);
}
