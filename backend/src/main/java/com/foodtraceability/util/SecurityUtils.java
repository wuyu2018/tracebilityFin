package com.foodtraceability.util;

import com.foodtraceability.entity.AgentIdentity;
import com.foodtraceability.entity.OffchainStorage;
import com.foodtraceability.repository.AdminRepository;
import com.foodtraceability.repository.AgentIdentityRepository;
import com.foodtraceability.repository.OffchainStorageRepository;
import com.foodtraceability.security.AccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SecurityUtils {

    private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);

    private final AdminRepository adminRepository;
    private final AgentIdentityRepository agentIdentityRepository;
    private final OffchainStorageRepository offchainStorageRepository;
    private final AccessControlService accessControlService;

    public SecurityUtils(AdminRepository adminRepository,
                         AgentIdentityRepository agentIdentityRepository,
                         OffchainStorageRepository offchainStorageRepository,
                         AccessControlService accessControlService) {
        this.adminRepository = adminRepository;
        this.agentIdentityRepository = agentIdentityRepository;
        this.offchainStorageRepository = offchainStorageRepository;
        this.accessControlService = accessControlService;
    }

    public Long getCurrentCompanyId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        if (isSuperAdmin(auth)) {
            return null;
        }
        return null;
    }

    public String getCurrentAgentType() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        if (isSuperAdmin(auth)) {
            return null;
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            String authStr = authority.getAuthority();
            if (authStr != null && authStr.startsWith("AGENT_TYPE_")) {
                return authStr.substring("AGENT_TYPE_".length());
            }
        }
        return null;
    }

    public boolean isSuperAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return isSuperAdmin(auth);
    }

    private boolean isSuperAdmin(Authentication auth) {
        if (auth == null) return false;
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if ("ROLE_SUPER_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAgentType(String agentType) {
        if (isSuperAdmin()) return true;
        String current = getCurrentAgentType();
        return agentType.equals(current);
    }

    public String getCurrentAgentId() {
        if (isSuperAdmin()) return null;
        String agentType = getCurrentAgentType();
        if (agentType == null) return null;
        List<AgentIdentity> identities = agentIdentityRepository.findByAgentTypeAndStatus(agentType, "ACTIVE");
        if (identities.isEmpty()) {
            log.warn("No active agent identity found for agentType: {}", agentType);
            return null;
        }
        return identities.get(0).getAgentId();
    }

    public boolean checkDataAccess(String storageKey, String action) {
        if (isSuperAdmin()) return true;
        String agentId = getCurrentAgentId();
        if (agentId == null) {
            log.warn("Cannot check data access: no agent ID for current user");
            return false;
        }
        List<OffchainStorage> storages = offchainStorageRepository.findByStorageKey(storageKey);
        if (storages.isEmpty()) {
            log.warn("No offchain storage found for storageKey: {}", storageKey);
            return true;
        }
        String foodId = storages.get(0).getFoodId();
        return accessControlService.hasPermission(foodId, action, agentId);
    }
}
