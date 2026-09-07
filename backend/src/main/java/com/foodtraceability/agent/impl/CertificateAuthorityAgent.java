package com.foodtraceability.agent.impl;

import com.foodtraceability.agent.core.AbstractAgent;
import com.foodtraceability.agent.credential.CertificateAuthority;
import com.foodtraceability.agent.credential.AgentCertificate;
import com.foodtraceability.agent.credential.MemberServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CertificateAuthorityAgent extends AbstractAgent {

    private static final Logger log = LoggerFactory.getLogger(CertificateAuthorityAgent.class);

    private final MemberServices memberServices;
    private final CertificateAuthority certificateAuthority;
    private boolean msVerified = false;

    public CertificateAuthorityAgent(MemberServices memberServices,
                                      CertificateAuthority certificateAuthority) {
        super("CA-" + System.currentTimeMillis(), AgentType.CERTIFICATE_AUTHORITY);
        this.memberServices = memberServices;
        this.certificateAuthority = certificateAuthority;
    }

    @Override
    public void initialize() {
        super.initialize();
        // Register with MS first (Member Services issues certificate to CA-Agent)
        String msToken = memberServices.issueCertificateToCA(getAgentId());
        this.msVerified = msToken != null && !msToken.isEmpty();

        if (msVerified) {
            setState(AgentState.ACTIVE);
            addMetadata("capability", "certificate_authority");
            addMetadata("service_type", "ca");
            addMetadata("is_primary", "true");
            addMetadata("ms_verified", "true");
            log.info("CA Agent initialized and verified by MS: {}", getAgentId());
        } else {
            setState(AgentState.SUSPENDED);
            log.error("CA Agent failed MS verification: {}", getAgentId());
        }
    }

    public AgentCertificate registerAgent(String agentId, String commonName, long validityDays) {
        if (!isAuthorized()) {
            throw new IllegalStateException("CA Agent not authorized");
        }

        // Verify with MS first before issuing certificate to agent
        if (!memberServices.validateCACertificate(getAgentId())) {
            throw new IllegalStateException("CA Agent certificate not valid with MemberServices");
        }

        log.info("Registering agent: {}, commonName: {} (via MS verification)", agentId, commonName);
        AgentCertificate cert = certificateAuthority.issueCertificate(agentId, commonName, validityDays);
        return cert;
    }

    public void revokeAgent(String agentId, String reason) {
        if (!isAuthorized()) {
            throw new IllegalStateException("CA Agent not authorized");
        }

        if (!memberServices.validateCACertificate(getAgentId())) {
            throw new IllegalStateException("CA Agent not verified by MemberServices");
        }

        log.warn("Revoking agent: {}, reason: {}", agentId, reason);
        certificateAuthority.revokeCertificate(agentId, reason);

        long currentScore = getCreditScore();
        if (currentScore < 50) {
            updateCreditScore(-100);
        }
    }

    public boolean validateAgent(String agentId) {
        if (!memberServices.validateCACertificate(getAgentId())) {
            return false;
        }
        return certificateAuthority.validateCertificate(agentId);
    }

    public void updateCreditForValidation(boolean valid) {
        if (valid) {
            updateCreditScore(2);
        } else {
            updateCreditScore(-5);
        }
    }

    public CertificateAuthority getCertificateAuthority() {
        return certificateAuthority;
    }

    @Override
    public void shutdown() {
        setState(AgentState.SUSPENDED);
        log.info("CA Agent shutdown: {}", getAgentId());
    }
}
