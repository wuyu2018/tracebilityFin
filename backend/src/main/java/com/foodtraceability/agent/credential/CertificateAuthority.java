package com.foodtraceability.agent.credential;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CertificateAuthority {
    
    private static final Logger log = LoggerFactory.getLogger(CertificateAuthority.class);
    
    private final Map<String, AgentCertificate> certificates;
    private final Map<String, CertificateRevocationList> crl;
    private final KeyPair caKeyPair;
    
    public CertificateAuthority() throws Exception {
        this.certificates = new ConcurrentHashMap<>();
        this.crl = new ConcurrentHashMap<>();
        
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        this.caKeyPair = keyGen.generateKeyPair();
        
        log.info("Certificate Authority initialized");
    }
    
    public AgentCertificate issueCertificate(String agentId, String commonName, long validityDays) {
        if (isRevoked(agentId)) {
            throw new IllegalStateException("Agent " + agentId + " is revoked");
        }
        
        KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate key pair", e);
        }
        
        KeyPair agentKeyPair = keyGen.generateKeyPair();
        
        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date();
        Date notAfter = new Date(System.currentTimeMillis() + validityDays * 24 * 60 * 60 * 1000);
        
        AgentCertificate agentCert = new AgentCertificate(
            serialNumber,
            agentId,
            commonName,
            notBefore,
            notAfter,
            agentKeyPair
        );
        
        certificates.put(agentId, agentCert);
        log.info("Certificate issued for agent: {}", agentId);
        
        return agentCert;
    }
    
    public void revokeCertificate(String agentId, String reason) {
        AgentCertificate cert = certificates.get(agentId);
        if (cert != null) {
            CertificateRevocationList revocationList = crl.computeIfAbsent(
                agentId.substring(0, 1), 
                k -> new CertificateRevocationList()
            );
            
            revocationList.addRevokedCertificate(cert.getSerialNumber(), reason);
            log.warn("Certificate revoked for agent: {}, reason: {}", agentId, reason);
        }
    }
    
    public boolean isRevoked(String agentId) {
        return crl.values().stream()
            .anyMatch(crl -> crl.isRevoked(agentId));
    }
    
    public AgentCertificate getCertificate(String agentId) {
        return certificates.get(agentId);
    }
    
    public boolean validateCertificate(String agentId) {
        AgentCertificate cert = certificates.get(agentId);
        if (cert == null) {
            return false;
        }
        
        if (isRevoked(agentId)) {
            return false;
        }
        
        Date now = new Date();
        return !now.before(cert.getNotBefore()) && !now.after(cert.getNotAfter());
    }
    
    public KeyPair getAgentKeyPair(String agentId) {
        AgentCertificate cert = certificates.get(agentId);
        return cert != null ? cert.getKeyPair() : null;
    }
    
    public Map<String, AgentCertificate> getAllCertificates() {
        return new ConcurrentHashMap<>(certificates);
    }
    
    public Map<String, CertificateRevocationList> getCRL() {
        return new ConcurrentHashMap<>(crl);
    }
}
