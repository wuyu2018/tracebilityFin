package com.foodtraceability.agent.credential;

import java.security.KeyPair;
import java.math.BigInteger;
import java.util.Date;

public class AgentCertificate {
    private final BigInteger serialNumber;
    private final String agentId;
    private final String commonName;
    private final Date notBefore;
    private final Date notAfter;
    private final KeyPair keyPair;
    
    public AgentCertificate(BigInteger serialNumber, String agentId, String commonName,
                           Date notBefore, Date notAfter, KeyPair keyPair) {
        this.serialNumber = serialNumber;
        this.agentId = agentId;
        this.commonName = commonName;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.keyPair = keyPair;
    }
    
    public BigInteger getSerialNumber() {
        return serialNumber;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public String getCommonName() {
        return commonName;
    }
    
    public Date getNotBefore() {
        return notBefore;
    }
    
    public Date getNotAfter() {
        return notAfter;
    }
    
    public KeyPair getKeyPair() {
        return keyPair;
    }
}
