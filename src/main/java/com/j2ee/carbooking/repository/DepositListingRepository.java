package com.j2ee.carbooking.repository;

import com.j2ee.carbooking.enums.DepositListingStatus;
import com.j2ee.carbooking.model.DepositListing;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DepositListingRepository extends MongoRepository<DepositListing, String> {
    List<DepositListing> findByStatus(DepositListingStatus status);
    List<DepositListing> findBySellerId(String sellerId);
    Optional<DepositListing> findByOrderId(String orderId);

    // Scheduler dùng cái này để tìm bài hết hạn
    List<DepositListing> findByStatusAndExpiredAtBefore(
        DepositListingStatus status,
        LocalDateTime now
    );

    // Kiểm tra đơn hàng đã có bài đăng chưa
    boolean existsByOrderIdAndStatusIn(String orderId, List<DepositListingStatus> statuses);

    // Lấy danh sách bài OPEN chưa hết hạn — dùng cho marketplace
    List<DepositListing> findByStatusAndExpiredAtAfter(
        DepositListingStatus status,
        LocalDateTime now
    );
}
