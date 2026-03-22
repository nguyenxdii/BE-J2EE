package com.j2ee.carbooking.model;

import com.j2ee.carbooking.enums.NotificationType;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String userId; // FK → users._id — người nhận thông báo

    private String title; // Tiêu đề ngắn: "Đơn hàng đã được xác nhận"

    private String message; // Nội dung chi tiết thông báo

    private NotificationType type;
    // ORDER: liên quan đơn hàng
    // WALLET: liên quan ví tiền
    // DEPOSIT_LISTING: liên quan suất cọc
    // VERIFY: liên quan duyệt CCCD/GPLX
    // Dùng để hiển thị icon khác nhau trên UI

    private String refId;
    // ID liên quan — user bấm vào thông báo sẽ điều hướng đến đây
    // Ví dụ: type=ORDER thì refId là orderId → redirect /orders/{orderId}

    private Boolean isRead = false;
    // false = chưa đọc → đếm để hiển thị badge số đỏ trên navbar
    // true = đã đọc → không đếm nữa

    @CreatedDate
    private LocalDateTime createdAt;
}