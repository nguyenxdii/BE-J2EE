package com.j2ee.carbooking.dto.response;

import com.j2ee.carbooking.enums.OrderStatus;
import com.j2ee.carbooking.enums.PaymentMethod;
import com.j2ee.carbooking.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OrderResponse {
    private String id;
    private String orderCode;

    // Thông tin xe (enrich từ vehicles)
    private String vehicleId;
    private String vehicleName;
    private String vehicleImage;
    private String vehicleBrand;
    private String licensePlate;

    // Thông tin đơn
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;

    // Tài chính
    private Double rentalPrice;
    private Double depositAmount;
    private Double totalAmount;

    // Trạng thái
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;

    // Suất cọc
    private Boolean isTransferred;
    private String originalUserId;

    // Ghi chú & thời gian
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nếu thanh toán Momo — trả về payUrl
    private String payUrl;
}
