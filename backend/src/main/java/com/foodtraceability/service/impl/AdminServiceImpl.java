package com.foodtraceability.service.impl;

import com.foodtraceability.dto.AdminLoginDTO;
import com.foodtraceability.dto.LoginResponseDTO;
import com.foodtraceability.entity.Admin;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.AdminRepository;
import com.foodtraceability.service.AdminService;
import com.foodtraceability.service.LoginAttemptService;
import com.foodtraceability.util.CaptchaStorage;
import com.foodtraceability.util.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CaptchaStorage captchaStorage;
    private final LoginAttemptService loginAttemptService;

    public AdminServiceImpl(AdminRepository adminRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           CaptchaStorage captchaStorage,
                           LoginAttemptService loginAttemptService) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.captchaStorage = captchaStorage;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public LoginResponseDTO login(AdminLoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        String captcha = loginDTO.getCaptcha();

        checkAccountLockStatus(username);
        validateCaptcha(username, captcha);
        Admin admin = findAdminByUsernameAndValidatePassword(username, password);

        loginAttemptService.loginSucceeded(username);

        return buildLoginResponse(admin);
    }

    private void checkAccountLockStatus(String username) {
        if (loginAttemptService.isLocked(username)) {
            long remainingSeconds = loginAttemptService.getRemainingLockTime(username);
            throw new BusinessException(String.format(
                "账号已被锁定，请 %d 分钟后再试",
                remainingSeconds / 60 + 1
            ));
        }
    }

    private void validateCaptcha(String username, String captcha) {
        if (captcha == null || captcha.isEmpty()) {
            throw new BusinessException("验证码不能为空");
        }
        String expectedCaptcha = captchaStorage.getCaptcha(username);
        if (expectedCaptcha == null || !expectedCaptcha.equalsIgnoreCase(captcha)) {
            loginAttemptService.loginFailed(username);
            throw new BusinessException("验证码错误");
        }
    }

    private Admin findAdminByUsernameAndValidatePassword(String username, String password) {
        Optional<Admin> adminOptional = adminRepository.findByUsername(username);
        if (adminOptional.isEmpty()) {
            loginAttemptService.loginFailed(username);
            throw new BusinessException("账号或密码错误");
        }
        Admin admin = adminOptional.get();
        if (!admin.validatePassword(password, passwordEncoder)) {
            loginAttemptService.loginFailed(username);
            throw new BusinessException("账号或密码错误");
        }
        return admin;
    }

    private LoginResponseDTO buildLoginResponse(Admin admin) {
        String token = jwtTokenProvider.generateToken(admin.getUsername(), admin.getRole(),
                admin.getAgentType());

        LoginResponseDTO response = new LoginResponseDTO();
        response.setUsername(admin.getUsername());
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenProvider.getExpirationTime() / 1000);
        response.setRole(admin.getRole());
        response.setAgentType(admin.getAgentType());

        return response;
    }

    @Override
    public Admin findByUsername(String username) {
        return adminRepository.findByUsername(username).orElse(null);
    }

    @Override
    public Admin createAdmin(String username, String password, String role, String agentType) {
        adminRepository.findByUsername(username)
            .ifPresent(existing -> {
                throw new BusinessException("管理员已存在");
            });

        Admin admin = Admin.create(username, passwordEncoder.encode(password));
        admin.setRole(role != null ? role : "ADMIN");
        admin.setAgentType(agentType);
        return adminRepository.save(admin);
    }

    @Override
    public java.util.List<Admin> listAllAdmins() {
        return adminRepository.findAll();
    }

    @Override
    public void verifyCurrentPassword(String username, String currentPassword) {
        validateUsernameAndPassword(username, currentPassword);

        Admin admin = adminRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException("管理员不存在"));

        if (!admin.validatePassword(currentPassword, passwordEncoder)) {
            throw new BusinessException("当前密码验证失败");
        }
    }

    @Override
    public void deleteAdmin(Long id, String currentUsername) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new BusinessException("管理员不存在"));
        if (admin.getUsername().equals(currentUsername)) {
            throw new BusinessException("不能删除自己");
        }
        adminRepository.delete(admin);
    }

    private void validateUsernameAndPassword(String username, String currentPassword) {
        if (username == null || username.isBlank()) {
            throw new BusinessException("用户名不能为空");
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new BusinessException("当前密码不能为空");
        }
    }
}
