package com.foodtraceability.service;

import com.foodtraceability.dto.AdminLoginDTO;
import com.foodtraceability.dto.LoginResponseDTO;
import com.foodtraceability.entity.Admin;

public interface AdminService {
    
    LoginResponseDTO login(AdminLoginDTO loginDTO);
    
    Admin findByUsername(String username);
    
    Admin createAdmin(String username, String password, String role, String agentType);
    
    void verifyCurrentPassword(String username, String currentPassword);
    
    java.util.List<Admin> listAllAdmins();
    
    void deleteAdmin(Long id, String currentUsername);
}
