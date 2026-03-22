package com.j2ee.carbooking.enums;

// Trạng thái giao dịch ví
public enum TransactionStatus {
    PENDING, // Đang chờ callback từ VNPay/Momo xác nhận
    SUCCESS, // Giao dịch thành công
    FAILED   // Giao dịch thất bại
}