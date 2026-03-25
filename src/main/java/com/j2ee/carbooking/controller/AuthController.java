package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.request.*;
import com.j2ee.carbooking.dto.response.AppApiResponse;
import com.j2ee.carbooking.dto.response.AuthResponse;
import com.j2ee.carbooking.service.AuthService;
import com.j2ee.carbooking.service.GoogleAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    // POST /api/auth/google — Đăng nhập bằng Google
    @PostMapping("/google")
    public ResponseEntity<AppApiResponse<AuthResponse>> googleLogin(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");
        AuthResponse data = googleAuthService.loginWithGoogle(idToken);
        return ResponseEntity.ok(AppApiResponse.success("Đăng nhập Google thành công", data));
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<AppApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse data = authService.register(request);
        return ResponseEntity.ok(AppApiResponse.success("Đăng ký thành công", data));
    }

    // POST /api/auth/register/send-otp — Bước 1: Gửi OTP
    @PostMapping("/register/send-otp")
    public ResponseEntity<AppApiResponse<?>> sendRegisterOtp(
            @Valid @RequestBody RegisterRequest request) {
        authService.sendRegisterOtp(request);
        return ResponseEntity.ok(AppApiResponse.success("Mã OTP đã được gửi đến " + request.getEmail()));
    }

    // POST /api/auth/register/verify-otp — Bước 2: Xác minh OTP + tạo tài khoản
    @PostMapping("/register/verify-otp")
    public ResponseEntity<AppApiResponse<AuthResponse>> verifyRegisterOtp(
            @RequestBody RegisterRequest request,
            @RequestParam String otp) {
        AuthResponse data = authService.verifyRegisterOtp(request, otp);
        return ResponseEntity.ok(AppApiResponse.success("Đăng ký thành công", data));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AppApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse data = authService.login(request);
        return ResponseEntity.ok(AppApiResponse.success("Đăng nhập thành công", data));
    }

    // PUT /api/auth/change-password — cần đăng nhập
    @PostMapping("/change-password")
    public ResponseEntity<AppApiResponse<String>> changePassword(
            @RequestAttribute String userId,
            @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ResponseEntity.ok(AppApiResponse.success("Đổi mật khẩu thành công"));
    }

    // POST /api/auth/forgot-password — Gửi link reset
    @PostMapping("/forgot-password")
    public ResponseEntity<AppApiResponse<?>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(AppApiResponse.success("Link đặt lại mật khẩu đã được gửi đến email của bạn"));
    }

    // POST /api/auth/reset-password — Đặt lại mật khẩu
    @PostMapping("/reset-password")
    public ResponseEntity<AppApiResponse<?>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(AppApiResponse.success("Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại"));
    }
}
