package com.foodtraceability.config;

import com.foodtraceability.entity.Admin;
import com.foodtraceability.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final AdminService adminService;

    @Value("${app.default-admin.username}")
    private String defaultAdminUsername;

    @Value("${app.default-admin.password}")
    private String defaultAdminPassword;

    public AdminInitializer(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    public void run(String... args) {
        if (defaultAdminUsername == null || defaultAdminUsername.isBlank()
                || defaultAdminPassword == null || defaultAdminPassword.isBlank()) {
            log.info("DEFAULT_ADMIN_USERNAME/PASSWORD not set, skipping admin initialization");
            return;
        }

        try {
            Admin existing = adminService.findByUsername(defaultAdminUsername);
            if (existing != null) {
                log.info("Default admin [{}] already exists, skipping", defaultAdminUsername);
                return;
            }

            adminService.createAdmin(defaultAdminUsername, defaultAdminPassword, "SUPER_ADMIN", null);
            log.info("Default admin [{}] created successfully with role SUPER_ADMIN", defaultAdminUsername);
        } catch (Exception e) {
            log.error("Failed to initialize default admin: {}", e.getMessage());
        }
    }
}
