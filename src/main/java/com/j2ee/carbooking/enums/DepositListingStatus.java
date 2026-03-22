package com.j2ee.carbooking.enums;

// Trạng thái bài đăng bán suất cọc
public enum DepositListingStatus {
    OPEN,      // Đang hiển thị trên marketplace, chờ người mua
    SOLD,      // Đã có người mua thành công
    EXPIRED,   // Hết hạn (quá 24h trước ngày nhận xe), scheduler tự đổi
    CANCELLED  // A tự xoá bài đăng
}