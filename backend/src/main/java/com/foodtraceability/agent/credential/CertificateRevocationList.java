package com.foodtraceability.agent.credential;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CertificateRevocationList {
    private final Map<BigInteger, RevocationEntry> revokedCertificates;
    
    public CertificateRevocationList() {
        this.revokedCertificates = new ConcurrentHashMap<>();
    }
    
    public void addRevokedCertificate(BigInteger serialNumber, String reason) {
        revokedCertificates.put(serialNumber, new RevocationEntry(serialNumber, reason));
    }
    
    public boolean isRevoked(BigInteger serialNumber) {
        return revokedCertificates.containsKey(serialNumber);
    }
    
    public boolean isRevoked(String agentId) {
        return revokedCertificates.values().stream()
            .anyMatch(entry -> entry.getAgentId() != null);
    }
    
    public List<RevocationEntry> getRevokedCertificates() {
        return new ArrayList<>(revokedCertificates.values());
    }
    
    public static class RevocationEntry {
        private final BigInteger serialNumber;
        private final String reason;
        private final Instant revocationDate;
        private String agentId;
        
        public RevocationEntry(BigInteger serialNumber, String reason) {
            this.serialNumber = serialNumber;
            this.reason = reason;
            this.revocationDate = Instant.now();
        }
        
        public BigInteger getSerialNumber() {
            return serialNumber;
        }
        
        public String getReason() {
            return reason;
        }
        
        public Instant getRevocationDate() {
            return revocationDate;
        }
        
        public String getAgentId() {
            return agentId;
        }
        
        public void setAgentId(String agentId) {
            this.agentId = agentId;
        }
    }
}
