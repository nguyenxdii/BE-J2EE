package com.j2ee.carbooking.service;

import com.j2ee.carbooking.enums.NotificationType;
import com.j2ee.carbooking.model.Notification;
import com.j2ee.carbooking.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Method dùng chung — các service khác gọi vào đây để tạo thông báo
    // Ví dụ: notificationService.create(userId, "Tiêu đề", "Nội dung", NotificationType.ORDER, orderId)
    public void create(String userId, String title, String message,
                       NotificationType type, String refId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRefId(refId);
        notification.setIsRead(false);
        notificationRepository.save(notification);
    }

    // Lấy danh sách thông báo của user
    public List<Notification> getByUserId(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Đếm thông báo chưa đọc — FE dùng để hiển thị badge đỏ
    public long countUnread(String userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    // Đánh dấu một thông báo đã đọc
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    // Đánh dấu tất cả thông báo của user đã đọc
    public void markAllAsRead(String userId) {
        List<Notification> list = notificationRepository
            .findByUserIdOrderByCreatedAtDesc(userId);
        list.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(list);
    }
}
