package com.foodtraceability.traceability.interfaces.dto;

import com.foodtraceability.traceability.application.dto.UpdateMaterialRequest;

public class UpdateMaterialVarietyRequest {
    private String name;
    private Boolean isActive;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public UpdateMaterialRequest toAppRequest() {
        return new UpdateMaterialRequest(name, isActive);
    }
}
