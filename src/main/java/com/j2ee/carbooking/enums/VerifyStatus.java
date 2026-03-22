package com.j2ee.carbooking.enums;

// Trạng thái xác minh CCCD/GPLX
public enum VerifyStatus {
    PENDING,  // Đã upload, chờ admin duyệt
    VERIFIED, // Đã được duyệt, có thể đặt xe
    REJECTED  // Bị từ chối, cần upload lại
}