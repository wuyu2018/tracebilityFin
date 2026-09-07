package com.foodtraceability.agent.credential;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MemberServices {

    private static final Logger log = LoggerFactory.getLogger(MemberServices.class);

    private final KeyPair masterKeyPair;
    private final Map<String, String> caCertificates;

    public MemberServices() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            this.masterKeyPair = keyGen.generateKeyPair();
            this.caCertificates = new ConcurrentHashMap<>();
            log.info("MemberServices initialized with master RSA key pair");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MemberServices", e);
        }
    }

    public String issueCertificateToCA(String caAgentId) {
        if (caCertificates.containsKey(caAgentId)) {
            log.warn("CA-Agent {} already has a certificate, re-issuing", caAgentId);
        }

        String certSerial = "MS-CA-" + caAgentId + "-" + System.currentTimeMillis();
        String certToken = Integer.toHexString((certSerial + masterKeyPair.getPublic().hashCode()).hashCode());

        caCertificates.put(caAgentId, certToken);
        log.info("MemberServices issued certificate to CA-Agent: {} (serial={})", caAgentId, certSerial);
        return certToken;
    }

    public boolean validateCACertificate(String caAgentId) {
        if (!caCertificates.containsKey(caAgentId)) {
            log.warn("CA-Agent {} has no valid MS certificate", caAgentId);
            return false;
        }
        return true;
    }

    public void revokeCACertificate(String caAgentId, String reason) {
        if (caCertificates.remove(caAgentId) != null) {
            log.warn("MemberServices revoked CA-Agent certificate: {}, reason: {}", caAgentId, reason);
        }
    }

    public KeyPair getMasterKeyPair() {
        return masterKeyPair;
    }
}
