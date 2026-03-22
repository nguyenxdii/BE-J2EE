package com.j2ee.carbooking.model;

import com.j2ee.carbooking.enums.TransactionStatus;
import com.j2ee.carbooking.enums.TransactionType;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "walletTransactions")
public class WalletTransaction {

    @Id
    private String id;

    private String userId; // FK → users._id — chủ ví thực hiện giao dịch

    private TransactionType type;
    // DEPOSIT: nạp tiền vào ví
    // PAY: trừ tiền khi đặt xe hoặc mua suất cọc
    // REFUND: hoàn tiền khi admin huỷ đơn
    // RECEIVE: nhận tiền khi bán suất cọc thành công

    private Double amount; // Số tiền giao dịch — luôn dương, chiều tăng/giảm xác định bởi type

    private Double balanceBefore; // Số dư ví trước giao dịch — dùng để đối soát nếu có lỗi

    private Double balanceAfter; // Số dư ví sau giao dịch

    private String refType;
    // Loại đối tượng liên quan: "ORDER" hoặc "DEPOSIT_LISTING"
    // Dùng để biết refId trỏ tới collection nào

    private String refId;
    // ID của đơn hàng hoặc bài đăng suất cọc liên quan
    // User bấm vào giao dịch sẽ điều hướng đến đây

    private String description;
    // Mô tả hiển thị cho user:
    // "Thanh toán cọc đơn ORD-001"
    // "Nhận tiền bán suất cọc xe Honda Wave"
    // "Hoàn tiền đơn bị huỷ ORD-002"

    private TransactionStatus status = TransactionStatus.PENDING;
    // PENDING: chờ callback từ VNPay/Momo
    // SUCCESS: giao dịch thành công
    // FAILED: giao dịch thất bại

    @CreatedDate
    private LocalDateTime createdAt;
    // Không có updatedAt vì walletTransaction không bao giờ sửa
    // Chỉ thêm mới, không xoá, không sửa
}