package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.request.*;
import com.j2ee.carbooking.dto.response.AppApiResponse;
import com.j2ee.carbooking.dto.response.AuthResponse;
import com.j2ee.carbooking.service.AuthService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<AppApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse data = authService.register(request);
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
}
