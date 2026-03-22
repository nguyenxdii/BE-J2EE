package com.j2ee.carbooking.repository;

import com.j2ee.carbooking.enums.VehicleStatus;
import com.j2ee.carbooking.model.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    boolean existsByLicensePlate(String licensePlate);
    List<Vehicle> findByCategoryId(String categoryId);
    List<Vehicle> findByStatus(VehicleStatus status);
    // Tìm xe nổi bật cho trang chủ — lấy top avgRating
    List<Vehicle> findTop8ByStatusOrderByAvgRatingDesc(VehicleStatus status);
}
