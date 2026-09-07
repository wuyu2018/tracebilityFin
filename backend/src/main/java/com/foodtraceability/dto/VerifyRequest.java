package com.foodtraceability.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyRequest {

    @NotBlank(message = "防伪码不能为空")
    @Size(min = 12, max = 20, message = "防伪码长度应为12-20位")
    private String antiFakeCode;
    
    private String batchNumber;
}
