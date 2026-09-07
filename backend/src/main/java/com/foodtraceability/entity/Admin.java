package com.foodtraceability.entity;

import com.foodtraceability.exception.BusinessException;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "role", length = 20)
    private String role = "ADMIN";

    @Column(name = "agent_type", length = 20)
    private String agentType;  // PRODUCTION / CIRCULATION / SALES / null

    public boolean validatePassword(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.password);
    }

    public static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new BusinessException("用户名不能为空");
        }
        if (!username.matches("^[a-zA-Z0-9]{4,20}$")) {
            throw new BusinessException("用户名必须为4-20位字母或数字组合");
        }
    }

    public static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException("密码不能为空");
        }
        if (password.length() < 8) {
            throw new BusinessException("密码长度不能少于8位");
        }
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$")) {
            throw new BusinessException("密码必须包含字母、数字和特殊字符");
        }
    }

    public static Admin create(String username, String encodedPassword) {
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(encodedPassword);
        return admin;
    }
}