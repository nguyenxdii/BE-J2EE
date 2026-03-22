package com.j2ee.carbooking.enums;

// Trạng thái xe
public enum VehicleStatus {
    AVAILABLE, // Có thể đặt
    RENTING,   // Đang được thuê
    HIDDEN     // Admin ẩn tạm thời, không hiện trên tìm kiếm
}