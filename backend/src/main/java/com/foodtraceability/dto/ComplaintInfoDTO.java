package com.foodtraceability.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintInfoDTO {

    private List<ComplaintInfo> complaintInfos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplaintInfo {
        private Long id;
        private String complaintReason;
        private LocalDateTime complaintTime;
        private String productName;
        private String batchNumber;
    }
}