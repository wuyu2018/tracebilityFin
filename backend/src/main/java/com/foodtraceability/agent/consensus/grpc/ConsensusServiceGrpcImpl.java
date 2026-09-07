package com.foodtraceability.agent.consensus.grpc;

import com.foodtraceability.agent.consensus.PbftConsensus;
import com.foodtraceability.agent.consensus.PbftMessage;
import com.foodtraceability.agent.consensus.transport.ConsensusTransport;
import com.foodtraceability.agent.contract.DataOnChainContract;
import com.foodtraceability.agent.contract.PermissionControlContract;
import com.foodtraceability.service.BlockchainService;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class ConsensusServiceGrpcImpl extends ConsensusServiceGrpc.ConsensusServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(ConsensusServiceGrpcImpl.class);
    private static final Duration CONSENSUS_TIMEOUT = Duration.ofSeconds(30);

    private final PbftConsensus pbftConsensus;
    private final ConsensusPeerConfig peerConfig;
    private final PermissionControlContract permissionContract;
    private final DataOnChainContract dataContract;
    private final BlockchainService blockchainService;

    public ConsensusServiceGrpcImpl(PbftConsensus pbftConsensus, ConsensusPeerConfig peerConfig,
                                     PermissionControlContract permissionContract, DataOnChainContract dataContract,
                                     BlockchainService blockchainService) {
        this.pbftConsensus = pbftConsensus;
        this.peerConfig = peerConfig;
        this.permissionContract = permissionContract;
        this.dataContract = dataContract;
        this.blockchainService = blockchainService;
    }

    @Override
    public void initiateConsensus(ConsensusRequestProto request,
                                   StreamObserver<ConsensusResponseProto> responseObserver) {
        try {
            String context = request.getContext();
            String digest = request.getDigest();

            log.info("gRPC: received InitiateConsensus - digest={}", digest);

            long seqNum = pbftConsensus.createAndPersistRequest(digest);
            PbftMessage prePrepare = pbftConsensus.createPrePrepare(seqNum, digest);
            pbftConsensus.broadcastPrePrepareViaTransport(prePrepare);

            Instant deadline = Instant.now().plus(CONSENSUS_TIMEOUT);
            while (Instant.now().isBefore(deadline)) {
                if (pbftConsensus.canExecute(seqNum)) {
                    pbftConsensus.markExecuted(seqNum);
                    ConsensusResponseProto response = ConsensusResponseProto.newBuilder()
                            .setSuccess(true)
                            .setSequenceNumber(seqNum)
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                    return;
                }
                Thread.sleep(100);
            }

            ConsensusResponseProto response = ConsensusResponseProto.newBuilder()
                    .setSuccess(false)
                    .setMessage("Consensus timeout: seq=" + seqNum)
                    .setSequenceNumber(seqNum)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("InitiateConsensus failed", e);
            responseObserver.onNext(ConsensusResponseProto.newBuilder()
                    .setSuccess(false).setMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void endorse(EndorsementRequestProto request,
                         StreamObserver<EndorsementResponseProto> responseObserver) {
        try {
            String context = request.getContext();
            String agentId = peerConfig.getAgentId();

            boolean approved = true;
            String reason = "";

            String permContext = agentId + "|WRITE";
            if (!permissionContract.validate(permContext)) {
                approved = false;
                reason = "Permission denied";
            }

            if (approved && !dataContract.validate(context)) {
                approved = false;
                reason = "Data validation failed";
            }

            String signature = blockchainService.sign(request.getDigest());

            responseObserver.onNext(EndorsementResponseProto.newBuilder()
                    .setAgentId(agentId)
                    .setApproved(approved)
                    .setSignature(signature)
                    .setReason(reason)
                    .build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Endorse failed", e);
            responseObserver.onNext(EndorsementResponseProto.newBuilder()
                    .setAgentId(peerConfig.getAgentId())
                    .setApproved(false)
                    .setReason(e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void sendPrePrepare(PbftMessageProto request,
                                StreamObserver<AckResponseProto> responseObserver) {
        try {
            PbftMessage msg = PbftMessageConverter.fromProto(request);
            log.debug("gRPC: received PRE_PREPARE - seq={}, from={}",
                    msg.getSequenceNumber(), msg.getSenderId());
            pbftConsensus.handleIncomingPrePrepare(msg);
            responseObserver.onNext(AckResponseProto.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("sendPrePrepare failed", e);
            responseObserver.onNext(AckResponseProto.newBuilder().setSuccess(false).setMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void sendPrepare(PbftMessageProto request,
                             StreamObserver<AckResponseProto> responseObserver) {
        try {
            PbftMessage msg = PbftMessageConverter.fromProto(request);
            log.debug("gRPC: received PREPARE - seq={}, from={}",
                    msg.getSequenceNumber(), msg.getSenderId());
            pbftConsensus.handleIncomingPrepare(msg);
            responseObserver.onNext(AckResponseProto.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("sendPrepare failed", e);
            responseObserver.onNext(AckResponseProto.newBuilder().setSuccess(false).setMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void sendCommit(PbftMessageProto request,
                            StreamObserver<AckResponseProto> responseObserver) {
        try {
            PbftMessage msg = PbftMessageConverter.fromProto(request);
            log.debug("gRPC: received COMMIT - seq={}, from={}",
                    msg.getSequenceNumber(), msg.getSenderId());
            pbftConsensus.handleIncomingCommit(msg);
            responseObserver.onNext(AckResponseProto.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("sendCommit failed", e);
            responseObserver.onNext(AckResponseProto.newBuilder().setSuccess(false).setMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void notifyBlock(BlockNotificationProto request,
                             StreamObserver<AckResponseProto> responseObserver) {
        try {
            log.info("gRPC: received NotifyBlock - hash={}, foodId={}",
                    request.getBlockHash(), request.getFoodId());
            pbftConsensus.handleBlockNotification(
                    request.getBlockHash(), request.getFoodId(),
                    request.getChainType(), request.getBlockHeaderId());
            responseObserver.onNext(AckResponseProto.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("notifyBlock failed", e);
            responseObserver.onNext(AckResponseProto.newBuilder().setSuccess(false).setMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }
}
