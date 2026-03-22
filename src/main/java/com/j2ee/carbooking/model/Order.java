package com.j2ee.carbooking.model;

import com.j2ee.carbooking.enums.OrderStatus;
import com.j2ee.carbooking.enums.PaymentMethod;
import com.j2ee.carbooking.enums.PaymentStatus;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    private String orderCode; // Mã đơn hiển thị: "ORD-20240103-001" — sinh khi tạo đơn

    private String userId; // FK → users._id — người đang sở hữu đơn (có thể là B sau sang nhượng)

    private String vehicleId; // FK → vehicles._id

    private LocalDate startDate; // Ngày bắt đầu thuê, cũng là ngày đến nhận xe tại shop

    private LocalDate endDate; // Ngày trả xe

    private Integer totalDays; // Số ngày thuê = endDate - startDate

    private Double rentalPrice; // Tiền thuê = totalDays × pricePerDay

    private Double depositAmount; // Tiền cọc — snapshot tại thời điểm đặt, không đổi dù xe thay giá sau

    private Double totalAmount; // Tổng = rentalPrice + depositAmount

    private OrderStatus status = OrderStatus.PENDING; // Mặc định PENDING khi vừa tạo

    private PaymentStatus paymentStatus = PaymentStatus.UNPAID; // Mặc định UNPAID

    private PaymentMethod paymentMethod; // WALLET / VNPAY / MOMO

    private String note; // Ghi chú của user khi đặt xe

    // --- Phục vụ tính năng suất cọc ---

    private String originalUserId;
    // Lưu user A gốc khi đơn bị sang nhượng cho B
    // userId đổi sang B nhưng field này vẫn giữ A
    // Dùng để: hoàn tiền đúng cho A, ghi lịch sử sang nhượng

    private Boolean isTransferred = false;
    // true = đơn này đã qua sang nhượng
    // Dùng để hiển thị badge "Mua lại" trên trang chi tiết đơn

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}