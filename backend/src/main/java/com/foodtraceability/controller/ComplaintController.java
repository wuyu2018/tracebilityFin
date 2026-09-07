package com.foodtraceability.controller;

import com.foodtraceability.aop.OperationLog;
import com.foodtraceability.dto.ComplaintDTO;
import com.foodtraceability.service.ComplaintService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ComplaintController {

    private static final Logger log = LoggerFactory.getLogger(ComplaintController.class);

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping("/complaint")
    @OperationLog(entityType = "COMPLAINT", action = "CREATE")
    public ResponseEntity<?> createComplaint(@Valid @RequestBody ComplaintDTO complaintDTO) {
        log.info("[投诉提交] 新增投诉 - 防伪码: {}, 投诉原因: {}", 
            complaintDTO.getAntiFakeCode(), complaintDTO.getComplaintReason());
        long startTime = System.currentTimeMillis();
        
        try {
            ComplaintDTO createdComplaint = complaintService.createComplaint(complaintDTO);
            long duration = System.currentTimeMillis() - startTime;
            log.info("[投诉提交] 投诉成功 - 防伪码: {}, 耗时: {}ms", createdComplaint.getAntiFakeCode(), duration);
            return new ResponseEntity<>(createdComplaint, HttpStatus.CREATED);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[投诉提交] 系统错误 - 防伪码: {}, 错误: {}, 耗时: {}ms", 
                complaintDTO.getAntiFakeCode(), e.getMessage(), duration);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "提交失败，请稍后重试"));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {
        log.warn("[参数验证] 验证失败 - 错误数量: {}", ex.getBindingResult().getErrorCount());

        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
            log.debug("[参数验证] 字段错误 - {}: {}", fieldName, message);
        });

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", "参数验证失败");
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }
}
