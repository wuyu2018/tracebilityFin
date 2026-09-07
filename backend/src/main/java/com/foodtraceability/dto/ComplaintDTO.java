package com.foodtraceability.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintDTO {
    private Long id;

    @NotBlank(message = "防伪码不能为空")
    private String antiFakeCode;

    @NotBlank(message = "投诉原因不能为空")
    private String complaintReason;

    private LocalDateTime complaintTime;

    private String productName;
    private String batchNumber;
}
