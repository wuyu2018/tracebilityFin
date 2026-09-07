package com.foodtraceability.traceability.interfaces.dto;

import com.foodtraceability.traceability.application.dto.CreateMaterialRequest;

public class CreateMaterialVarietyRequest {
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CreateMaterialRequest toAppRequest() {
        return new CreateMaterialRequest(name);
    }
}
