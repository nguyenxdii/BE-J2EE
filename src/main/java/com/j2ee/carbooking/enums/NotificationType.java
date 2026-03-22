package com.j2ee.carbooking.enums;

// Loại thông báo — dùng để hiển thị icon khác nhau trên UI
public enum NotificationType {
    ORDER,           // Liên quan đến đơn hàng thuê xe
    WALLET,          // Liên quan đến ví (nạp tiền, hoàn tiền)
    DEPOSIT_LISTING, // Liên quan đến suất cọc
    VERIFY           // Liên quan đến duyệt CCCD/GPLX
}