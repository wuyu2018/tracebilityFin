package com.foodtraceability.traceability.interfaces.dto;

import com.foodtraceability.traceability.application.dto.MaterialResponse;

public class MaterialVarietyResponse {
    private final Long id;
    private final String name;
    private final boolean isActive;
    private final String createdAt;
    private final String updatedAt;

    public MaterialVarietyResponse(Long id, String name, boolean isActive,
                                   String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MaterialVarietyResponse from(MaterialResponse r) {
        return new MaterialVarietyResponse(
                r.id(), r.name(), r.isActive(),
                r.createdAt() != null ? r.createdAt().toString() : null,
                r.updatedAt() != null ? r.updatedAt().toString() : null);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public boolean getIsActive() { return isActive; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
