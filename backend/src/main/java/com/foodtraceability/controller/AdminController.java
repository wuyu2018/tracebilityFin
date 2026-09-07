package com.foodtraceability.controller;

import com.foodtraceability.aop.OperationLog;
import com.foodtraceability.dto.AdminLoginDTO;
import com.foodtraceability.dto.LoginResponseDTO;
import com.foodtraceability.entity.Admin;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.service.AdminService;
import com.foodtraceability.util.CaptchaStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    @Autowired
    private CaptchaStorage captchaStorage;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/captcha")
    public ResponseEntity<?> storeCaptcha(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String captcha = request.get("captcha");
        if (username == null || captcha == null) {
            return ResponseEntity.badRequest().body("username和captcha不能为空");
        }
        captchaStorage.setCaptcha(username, captcha);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> listAdmins() {
        var admins = adminService.listAllAdmins();
        var result = admins.stream().map(a -> {
            var map = new java.util.HashMap<String, Object>();
            map.put("id", a.getId());
            map.put("username", a.getUsername());
            map.put("role", a.getRole() != null ? a.getRole() : "ADMIN");
            map.put("agentType", a.getAgentType() != null ? a.getAgentType() : "");
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/admins/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id) {
        String currentUser = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        adminService.deleteAdmin(id, currentUser);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/login")
    @OperationLog(entityType = "ADMIN", action = "LOGIN")
    public ResponseEntity<?> login(@Valid @RequestBody AdminLoginDTO loginDTO) {
        log.info("[管理员登录] 登录尝试 - 用户名: {}", loginDTO.getUsername());
        long startTime = System.currentTimeMillis();
        
        try {
            LoginResponseDTO response = adminService.login(loginDTO);
            captchaStorage.removeCaptcha(loginDTO.getUsername());
            long duration = System.currentTimeMillis() - startTime;
            log.info("[管理员登录] 登录成功 - 用户名: {}, 耗时: {}ms", loginDTO.getUsername(), duration);
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("[管理员登录] 登录失败 - 用户名: {}, 原因: {}, 耗时: {}ms", 
                loginDTO.getUsername(), e.getMessage(), duration);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/admin/register")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @OperationLog(entityType = "ADMIN", action = "REGISTER")
    public ResponseEntity<?> registerAdmin(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String role = request.get("role");
        String agentType = request.get("agentType");
        String currentPassword = request.get("currentPassword");
        String currentAdminUsername = request.get("currentAdminUsername");

        try {
            Admin.validateUsername(username);
            Admin.validatePassword(password);
            if (currentPassword == null || currentPassword.isBlank()) {
                return ResponseEntity.badRequest().body("请输入当前管理员密码进行身份验证");
            }
            if (currentAdminUsername == null || currentAdminUsername.isBlank()) {
                return ResponseEntity.badRequest().body("无法确定当前管理员身份");
            }

            adminService.verifyCurrentPassword(currentAdminUsername, currentPassword);

            Admin admin = adminService.createAdmin(username, password, role, agentType);
            log.info("[管理员注册] 创建成功 - 操作管理员: {}, 新管理员: {}", currentAdminUsername, username);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "管理员创建成功", "username", admin.getUsername()));
        } catch (BusinessException e) {
            log.warn("[管理员注册] 创建失败 - 操作管理员: {}, 原因: {}", currentAdminUsername, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
