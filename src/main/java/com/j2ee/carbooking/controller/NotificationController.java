package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.response.ApiResponse;
import com.j2ee.carbooking.model.Notification;
import com.j2ee.carbooking.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/notifications — lấy danh sách thông báo
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        List<Notification> list = notificationService.getByUserId(userId);
        long unread = notificationService.countUnread(userId);
        return ResponseEntity.ok(ApiResponse.success("OK",
            Map.of("notifications", list, "unreadCount", unread)));
    }

    // PUT /api/notifications/{id}/read — đánh dấu 1 thông báo đã đọc
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<?>> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đọc"));
    }

    // PUT /api/notifications/read-all — đánh dấu tất cả đã đọc
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<?>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Đã đọc tất cả"));
    }
}
