package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.request.UpdateProfileRequest;
import com.j2ee.carbooking.dto.response.AppApiResponse;
import com.j2ee.carbooking.model.User;
import com.j2ee.carbooking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users/profile — xem hồ sơ
    @GetMapping("/profile")
    public ResponseEntity<AppApiResponse<User>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(AppApiResponse.success("Lấy thông tin thành công", user));
    }

    // PUT /api/users/profile — cập nhật hồ sơ
    @PutMapping("/profile")
    public ResponseEntity<AppApiResponse<User>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart(value = "data", required = false) UpdateProfileRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        User user = userService.updateProfile(userDetails.getUsername(), request, avatar);
        return ResponseEntity.ok(AppApiResponse.success("Cập nhật thông tin thành công", user));
    }

    // POST /api/users/identity — upload CCCD/GPLX
    @PostMapping("/identity")
    public ResponseEntity<AppApiResponse<User>> uploadIdentity(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart(value = "cccdFront", required = false) MultipartFile cccdFront,
            @RequestPart(value = "cccdBack", required = false) MultipartFile cccdBack,
            @RequestPart(value = "drivingLicense", required = false) MultipartFile drivingLicense) {
        User user = userService.uploadIdentity(userDetails.getUsername(), cccdFront, cccdBack, drivingLicense);
        return ResponseEntity.ok(AppApiResponse.success("Upload giấy tờ thành công", user));
    }
}
