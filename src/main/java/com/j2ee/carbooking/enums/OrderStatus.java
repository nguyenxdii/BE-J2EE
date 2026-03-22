package com.j2ee.carbooking.enums;

// Trạng thái đơn hàng — đi theo thứ tự từ trên xuống
public enum OrderStatus {
    PENDING,    // Vừa đặt, chờ admin xác nhận
    CONFIRMED,  // Admin đã xác nhận, chờ user đến nhận xe
    RENTING,    // User đã nhận xe, đang thuê
    COMPLETED,  // User đã trả xe, đơn hoàn thành
    CANCELLED   // Đơn bị huỷ (bởi user hoặc admin)
}