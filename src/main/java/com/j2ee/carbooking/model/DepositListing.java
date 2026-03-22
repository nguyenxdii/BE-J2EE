package com.j2ee.carbooking.model;

import com.j2ee.carbooking.enums.DepositListingStatus;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "depositListings")
public class DepositListing {

    @Id
    private String id;

    private String sellerId; // FK → users._id — user A đăng bán

    private String orderId; // FK → orders._id — đơn hàng gốc đang bán suất

    private String vehicleId;
    // FK → vehicles._id
    // Lưu riêng để hiển thị thông tin xe trên marketplace
    // mà không cần join qua orders

    private Double originalDeposit; // Tiền cọc gốc A đã đóng (300k) — snapshot, không thay đổi

    private Double sellingPrice;
    // Giá bán = 60% của originalDeposit — hệ thống tự tính khi A đăng
    // Không cho A tự nhập để tránh bán giá cao hơn cọc gốc

    private Double platformFee;
    // Phần nền tảng giữ = originalDeposit - sellingPrice
    // Lưu lại để thống kê doanh thu từ suất cọc

    private LocalDateTime expiredAt;
    // = startDate của order - 24 tiếng
    // Scheduler chạy mỗi giờ quét field này
    // Nếu expiredAt < now() và status = OPEN → đổi thành EXPIRED

    private DepositListingStatus status = DepositListingStatus.OPEN; // Mặc định OPEN khi đăng

    private String buyerId; // FK → users._id — user B, null khi chưa có người mua

    private LocalDateTime soldAt; // Thời điểm B mua thành công — dùng cho thống kê

    @CreatedDate
    private LocalDateTime createdAt;
}