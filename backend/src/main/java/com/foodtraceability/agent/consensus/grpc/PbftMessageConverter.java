package com.foodtraceability.agent.consensus.grpc;

import com.foodtraceability.agent.consensus.Endorsement;
import com.foodtraceability.agent.consensus.PbftMessage;

public final class PbftMessageConverter {

    private PbftMessageConverter() {}

    public static PbftMessageProto toProto(PbftMessage msg) {
        return PbftMessageProto.newBuilder()
                .setType(msg.getType().name())
                .setView(msg.getView())
                .setSequenceNumber(msg.getSequenceNumber())
                .setDigest(msg.getDigest() != null ? msg.getDigest() : "")
                .setSenderId(msg.getSenderId())
                .setSignature(msg.getSignature() != null ? msg.getSignature() : "")
                .setTimestamp(msg.getTimestamp())
                .build();
    }

    public static PbftMessage fromProto(PbftMessageProto proto) {
        PbftMessage.MessageType type = PbftMessage.MessageType.valueOf(proto.getType());
        PbftMessage msg = new PbftMessage(
                type,
                proto.getView(),
                proto.getSequenceNumber(),
                proto.getDigest().isEmpty() ? null : proto.getDigest(),
                proto.getSenderId()
        );
        if (!proto.getSignature().isEmpty()) {
            msg.setSignature(proto.getSignature());
        }
        msg.setTimestamp(proto.getTimestamp());
        return msg;
    }

    public static Endorsement fromEndorsementProto(EndorsementResponseProto proto) {
        return new Endorsement(
                proto.getAgentId(),
                proto.getApproved(),
                proto.getSignature(),
                proto.getReason()
        );
    }

    public static EndorsementResponseProto toEndorsementProto(Endorsement endorsement) {
        return EndorsementResponseProto.newBuilder()
                .setAgentId(endorsement.agentId())
                .setApproved(endorsement.approved())
                .setSignature(endorsement.signature())
                .setReason(endorsement.reason())
                .build();
    }
}
