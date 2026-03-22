package com.j2ee.carbooking.repository;

import com.j2ee.carbooking.model.WalletTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WalletTransactionRepository extends MongoRepository<WalletTransaction, String> {
    // Lấy lịch sử giao dịch của user, mới nhất trước
    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(String userId);
}
