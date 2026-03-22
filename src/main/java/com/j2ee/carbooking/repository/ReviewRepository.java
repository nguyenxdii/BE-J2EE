package com.j2ee.carbooking.repository;

import com.j2ee.carbooking.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByVehicleId(String vehicleId);
    // Kiểm tra đơn đã review chưa — mỗi đơn chỉ review 1 lần
    boolean existsByOrderId(String orderId);
}
