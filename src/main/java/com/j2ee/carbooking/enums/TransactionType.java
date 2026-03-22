package com.j2ee.carbooking.enums;

// Loại giao dịch ví
public enum TransactionType {
    DEPOSIT, // Nạp tiền vào ví từ VNPay/Momo
    PAY,     // Trừ tiền ví khi thanh toán đặt xe hoặc mua suất cọc
    REFUND,  // Hoàn tiền vào ví khi admin huỷ đơn
    RECEIVE  // Nhận tiền vào ví khi bán suất cọc thành công
}