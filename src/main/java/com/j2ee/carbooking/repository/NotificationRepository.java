package com.j2ee.carbooking.repository;

import com.j2ee.carbooking.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    // Lấy tất cả thông báo của user, mới nhất trước
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    // Đếm thông báo chưa đọc — hiển thị badge đỏ trên navbar
    long countByUserIdAndIsRead(String userId, Boolean isRead);
}
