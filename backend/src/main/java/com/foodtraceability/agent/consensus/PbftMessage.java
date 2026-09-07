package com.foodtraceability.agent.consensus;

import java.util.List;

public class PbftMessage {
    private final MessageType type;
    private final String view;
    private final Long sequenceNumber;
    private final String digest;
    private final String senderId;
    private String signature;
    private long timestamp;
    
    public enum MessageType {
        REQUEST,
        PRE_PREPARE,
        PREPARE,
        COMMIT,
        REPLY
    }
    
    public PbftMessage(MessageType type, String view, Long sequenceNumber, String digest, String senderId) {
        this.type = type;
        this.view = view;
        this.sequenceNumber = sequenceNumber;
        this.digest = digest;
        this.senderId = senderId;
        this.signature = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    public MessageType getType() {
        return type;
    }
    
    public String getView() {
        return view;
    }
    
    public Long getSequenceNumber() {
        return sequenceNumber;
    }
    
    public String getDigest() {
        return digest;
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public PbftMessage copy() {
        PbftMessage copy = new PbftMessage(type, view, sequenceNumber, digest, senderId);
        copy.signature = this.signature;
        copy.timestamp = this.timestamp;
        return copy;
    }
}
