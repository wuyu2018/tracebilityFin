package com.foodtraceability.agent.contract;

import com.foodtraceability.agent.credential.CertificateAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PermissionControlContract implements SmartContract {
    
    private static final Logger log = LoggerFactory.getLogger(PermissionControlContract.class);
    
    private final CertificateAuthority certificateAuthority;
    
    public PermissionControlContract(CertificateAuthority certificateAuthority) {
        this.certificateAuthority = certificateAuthority;
    }
    
    @Override
    public String getContractId() {
        return "CONTRACT-PERMISSION-001";
    }
    
    @Override
    public ContractType getContractType() {
        return ContractType.PERMISSION_CONTROL;
    }
    
    @Override
    public boolean execute(String context) {
        log.debug("Executing permission control contract: {}", context);
        return true;
    }
    
    @Override
    public boolean validate(String context) {
        PermissionContext permissionContext = parseContext(context);
        
        if (permissionContext == null) {
            log.warn("Invalid permission context");
            return false;
        }
        
        boolean isValid = certificateAuthority.validateCertificate(permissionContext.agentId);
        
        if (!isValid) {
            log.warn("Agent {} failed permission validation", permissionContext.agentId);
            return false;
        }
        
        if (!hasRequiredPermission(permissionContext.agentId, permissionContext.requiredPermission)) {
            log.warn("Agent {} lacks required permission: {}", 
                    permissionContext.agentId, permissionContext.requiredPermission);
            return false;
        }
        
        log.debug("Agent {} permission validated successfully", permissionContext.agentId);
        return true;
    }
    
    private PermissionContext parseContext(String context) {
        try {
            String[] parts = context.split("\\|");
            if (parts.length < 2) {
                return null;
            }
            return new PermissionContext(parts[0], parts[1]);
        } catch (Exception e) {
            return null;
        }
    }
    
    private boolean hasRequiredPermission(String agentId, String requiredPermission) {
        if (requiredPermission == null) return false;

        String prefix = agentId.split("-")[0];

        switch (requiredPermission) {
            case "WRITE":
                boolean canWrite = "P".equals(prefix) || "C".equals(prefix)
                        || "S".equals(prefix) || "CA".equals(prefix) || "O".equals(prefix);
                if (!canWrite) {
                    log.warn("Agent {} (type={}) not allowed to WRITE", agentId, prefix);
                }
                return canWrite;
            case "READ":
                return true;
            default:
                log.warn("Unknown permission type: {} requested by agent {}", requiredPermission, agentId);
                return false;
        }
    }
    
    private static class PermissionContext {
        String agentId;
        String requiredPermission;
        
        PermissionContext(String agentId, String requiredPermission) {
            this.agentId = agentId;
            this.requiredPermission = requiredPermission;
        }
    }
}
