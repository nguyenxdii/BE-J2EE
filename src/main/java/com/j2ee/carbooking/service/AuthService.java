package com.j2ee.carbooking.service;

import com.j2ee.carbooking.dto.request.*;
import com.j2ee.carbooking.dto.response.AuthResponse;
import com.j2ee.carbooking.enums.Role;
import com.j2ee.carbooking.enums.UserStatus;
import com.j2ee.carbooking.model.User;
import com.j2ee.carbooking.repository.UserRepository;
import com.j2ee.carbooking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Đăng ký — điền form tạo tài khoản luôn
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setWalletBalance(0.0);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId());
        return new AuthResponse(token, user);
    }

    // Đăng nhập email/mật khẩu
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email hoặc mật khẩu không đúng");
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new RuntimeException("Tài khoản đã bị khoá");
        }

        String token = jwtUtil.generateToken(user.getId());
        return new AuthResponse(token, user);
    }

    // Đổi mật khẩu
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
