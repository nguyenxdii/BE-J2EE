package com.j2ee.carbooking.repository;

import com.j2ee.carbooking.enums.TransactionStatus;
import com.j2ee.carbooking.model.WalletTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository extends MongoRepository<WalletTransaction, String> {
    // Lấy lịch sử giao dịch của user, mới nhất trước
    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(String userId);

    // Lấy toàn bộ giao dịch cho Admin
    List<WalletTransaction> findAllByOrderByCreatedAtDesc();

    // Tìm transaction theo orderId và status — dùng cho callback
    Optional<WalletTransaction> findByRefIdAndStatus(String refId, TransactionStatus status);
}
