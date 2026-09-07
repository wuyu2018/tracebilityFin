package com.foodtraceability.agent.ledger;

import com.foodtraceability.agent.core.ServiceRecord;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

@Repository
public class ServiceLedger {
    
    private final Map<String, ServiceRecord> services = new ConcurrentHashMap<>();
    private final Map<String, Map<String, ServiceRecord>> agentServices = new ConcurrentHashMap<>();
    
    public void addService(ServiceRecord service) {
        services.put(service.getServiceId(), service);
        agentServices
            .computeIfAbsent(service.getAgentId(), k -> new ConcurrentHashMap<>())
            .put(service.getServiceId(), service);
    }
    
    public Optional<ServiceRecord> getService(String serviceId) {
        return Optional.ofNullable(services.get(serviceId));
    }
    
    public void updateService(String serviceId, ServiceRecord updatedService) {
        if (services.containsKey(serviceId)) {
            services.put(serviceId, updatedService);
            agentServices.get(updatedService.getAgentId()).put(serviceId, updatedService);
        }
    }
    
    public void removeService(String serviceId) {
        ServiceRecord service = services.get(serviceId);
        if (service != null) {
            service.setActive(false);
            services.put(serviceId, service);
            agentServices.get(service.getAgentId()).remove(serviceId);
        }
    }
    
    public Iterable<ServiceRecord> searchServices(String serviceType) {
        return services.values().stream()
            .filter(s -> s.isActive() && s.getServiceType().equals(serviceType))
            ::iterator;
    }
    
    public Iterable<ServiceRecord> getAgentServices(String agentId) {
        return agentServices.getOrDefault(agentId, new ConcurrentHashMap<>()).values();
    }
    
    public void deleteService(String serviceId) {
        ServiceRecord service = services.remove(serviceId);
        if (service != null) {
            agentServices.get(service.getAgentId()).remove(serviceId);
        }
    }
}
