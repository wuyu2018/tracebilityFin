package com.foodtraceability.controller;

import com.foodtraceability.dto.ComplaintInfoDTO;
import com.foodtraceability.service.GetAllComplaintInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('SUPER_ADMIN') or hasAuthority('AGENT_TYPE_SALES')")
public class GetAllComplaintInfoController {

    private static final Logger log = LoggerFactory.getLogger(GetAllComplaintInfoController.class);

    private final GetAllComplaintInfoService getAllComplaintInfo;

    public GetAllComplaintInfoController(GetAllComplaintInfoService getAllComplaintInfo) {
        this.getAllComplaintInfo = getAllComplaintInfo;
    }

    @GetMapping("/getAllComplaintInfo")
    public ResponseEntity<?> getAllComplaintInfo(ComplaintInfoDTO complaintInfoDTO) {
        log.debug("[投诉查询] 获取全部投诉信息");
        long startTime = System.currentTimeMillis();
        
        try {
            List<ComplaintInfoDTO.ComplaintInfo> result = getAllComplaintInfo.getAllComplaintInfo();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[投诉查询] 查询成功 - 记录数: {}, 耗时: {}ms", result.size(), duration);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[投诉查询] 查询失败 - 错误: {}, 耗时: {}ms", e.getMessage(), duration);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取投诉信息失败");
        }
    }

    @DeleteMapping("/deleteComplaintInfo/{id}")
    public ResponseEntity<?> deleteComplaintInfo(@PathVariable Long id) {
        log.info("[投诉删除] 删除投诉 - ID: {}", id);
        long startTime = System.currentTimeMillis();
        
        try {
            boolean deleted = getAllComplaintInfo.deleteComplaintInfo(id);
            long duration = System.currentTimeMillis() - startTime;
            
            if (deleted) {
                log.info("[投诉删除] 删除成功 - ID: {}, 耗时: {}ms", id, duration);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "删除成功");
                response.put("id", id);
                return ResponseEntity.ok(response);
            } else {
                log.warn("[投诉删除] 投诉不存在 - ID: {}, 耗时: {}ms", id, duration);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("投诉信息不存在，ID: " + id);
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[投诉删除] 删除失败 - ID: {}, 错误: {}, 耗时: {}ms", id, e.getMessage(), duration);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("删除投诉信息失败");
        }
    }

    @DeleteMapping("/deleteComplaintInfo/batch")
    public ResponseEntity<?> deleteComplaintInfoBatch(@RequestBody List<Long> ids) {
        log.info("[投诉批量删除] 开始批量删除 - 数量: {}", ids.size());
        long startTime = System.currentTimeMillis();
        
        try {
            int successCount = 0;
            int failCount = 0;

            for (Long id : ids) {
                try {
                    boolean deleted = getAllComplaintInfo.deleteComplaintInfo(id);
                    if (deleted) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    log.warn("[投诉批量删除] 单条删除失败 - ID: {}", id);
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("[投诉批量删除] 批量删除完成 - 成功: {}, 失败: {}, 耗时: {}ms", 
                successCount, failCount, duration);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("successCount", successCount);
            response.put("failCount", failCount);
            response.put("message", "批量删除完成，成功: " + successCount + "，失败: " + failCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[投诉批量删除] 批量删除失败 - 错误: {}, 耗时: {}ms", e.getMessage(), duration);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("批量删除投诉信息失败");
        }
    }
}
