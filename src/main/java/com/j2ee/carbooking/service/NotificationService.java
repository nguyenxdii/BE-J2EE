package com.j2ee.carbooking.service;

import com.j2ee.carbooking.enums.NotificationType;
import com.j2ee.carbooking.model.Notification;
import com.j2ee.carbooking.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

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

    public List<Notification> getByUserId(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long countUnread(String userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    public void markAllAsRead(String userId) {
        List<Notification> list = notificationRepository
            .findByUserIdOrderByCreatedAtDesc(userId);
        list.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(list);
    }
}
