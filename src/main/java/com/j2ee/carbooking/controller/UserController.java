package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.service.CloudinaryService; // Nhớ check đúng package service của bạn nhé
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    // THIẾU CÁI NÀY LÀ KHÔNG CHẠY ĐƯỢC ĐÂU DINO!
    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping("/balance")
public ResponseEntity<Long> getBalance() {
    // Giả sử lấy từ User hiện tại đang đăng nhập
    return ResponseEntity.ok(userService.getCurrentUser().getBalance());
}

    @PostMapping("/kyc/upload")
    public ResponseEntity<?> uploadKYC(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Kiểm tra file trống
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Vui lòng chọn một file ảnh!");
            }

            // 2. Gọi service đẩy ảnh lên Cloudinary và lấy URL trả về
            String imageUrl = cloudinaryService.uploadImage(file);
            
            // 3. Tạo phản hồi Mock Data
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("status", "PENDING"); // Trạng thái mặc định là Chờ duyệt
            response.put("message", "Tải ảnh lên thành công! Hồ sơ đang chờ xác minh.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}