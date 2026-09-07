package com.foodtraceability.dto;

import lombok.Data;
import java.util.List;

@Data
public class SecurityCodeGenerateResponse {
    private List<String> codes;
    private int count;
}