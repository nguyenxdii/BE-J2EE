package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.response.AppApiResponse;
import com.j2ee.carbooking.model.Notification;
import com.j2ee.carbooking.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // GET /api/notifications — lấy danh sách thông báo
    @GetMapping
    public ResponseEntity<AppApiResponse<Map<String, Object>>> getNotifications(@RequestAttribute String userId) {
        List<Notification> notifications = notificationService.getByUserId(userId);
        long unreadCount = notificationService.countUnread(userId);
        
        return ResponseEntity.ok(AppApiResponse.success("Lấy thông báo thành công", Map.of(
            "notifications", notifications,
            "unreadCount", unreadCount
        )));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<AppApiResponse<Void>> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(AppApiResponse.success("Đã đánh dấu đã đọc"));
    }

    @PutMapping("/read-all")
    public ResponseEntity<AppApiResponse<Void>> markAllAsRead(@RequestAttribute String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(AppApiResponse.success("Đã đánh dấu tất cả là đã đọc"));
    }
}
