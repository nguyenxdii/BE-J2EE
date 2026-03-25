package com.j2ee.carbooking.service;

import com.j2ee.carbooking.dto.request.*;
import com.j2ee.carbooking.dto.response.AuthResponse;
import com.j2ee.carbooking.enums.Role;
import com.j2ee.carbooking.enums.UserStatus;
import com.j2ee.carbooking.model.User;
import com.j2ee.carbooking.model.OtpToken;
import com.j2ee.carbooking.repository.OtpTokenRepository;
import com.j2ee.carbooking.repository.UserRepository;
import com.j2ee.carbooking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    // Đăng ký trực tiếp (không OTP)
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

    // ----------------------------------------------------------------
    // ĐĂNG KÝ BƯỚC 1: Gửi OTP về email
    // ----------------------------------------------------------------
    public void sendRegisterOtp(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        otpTokenRepository.deleteByEmailAndType(request.getEmail(), "REGISTER");

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));

        OtpToken otpToken = new OtpToken();
        otpToken.setEmail(request.getEmail());
        otpToken.setToken(otp);
        otpToken.setType("REGISTER");
        otpToken.setExpiredAt(LocalDateTime.now().plusMinutes(5));
        otpToken.setUsed(false);
        otpTokenRepository.save(otpToken);

        emailService.sendOtpRegister(request.getEmail(), otp);
    }

    // ----------------------------------------------------------------
    // ĐĂNG KÝ BƯỚC 2: Xác minh OTP → Tạo tài khoản
    // ----------------------------------------------------------------
    public AuthResponse verifyRegisterOtp(RegisterRequest request, String otp) {
        OtpToken otpToken = otpTokenRepository
            .findByEmailAndTokenAndType(request.getEmail(), otp, "REGISTER")
            .orElseThrow(() -> new RuntimeException("OTP không đúng hoặc đã hết hạn"));

        if (otpToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP đã hết hạn. Vui lòng yêu cầu mã mới");
        }

        if (otpToken.getUsed()) {
            throw new RuntimeException("OTP đã được sử dụng");
        }

        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

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

    // ----------------------------------------------------------------
    // QUÊN MẬT KHẨU: Gửi link reset về email
    // ----------------------------------------------------------------
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));

        otpTokenRepository.deleteByEmailAndType(request.getEmail(), "RESET");

        String resetToken = java.util.UUID.randomUUID().toString();

        OtpToken otpToken = new OtpToken();
        otpToken.setEmail(request.getEmail());
        otpToken.setToken(resetToken);
        otpToken.setType("RESET");
        otpToken.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        otpToken.setUsed(false);
        otpTokenRepository.save(otpToken);

        String resetLink = frontendUrl + "/reset-password?token=" + resetToken + "&email=" + request.getEmail();
        emailService.sendResetPassword(request.getEmail(), resetLink);
    }

    // ----------------------------------------------------------------
    // RESET MẬT KHẨU: Xác minh token → Đổi mật khẩu mới
    // ----------------------------------------------------------------
    public void resetPassword(ResetPasswordRequest request) {
        OtpToken otpToken = otpTokenRepository
            .findByEmailAndTokenAndType(request.getEmail(), request.getToken(), "RESET")
            .orElseThrow(() -> new RuntimeException("Link reset không hợp lệ hoặc đã hết hạn"));

        if (otpToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Link reset đã hết hạn. Vui lòng yêu cầu lại");
        }

        if (otpToken.getUsed()) {
            throw new RuntimeException("Link reset đã được sử dụng");
        }

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);
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
