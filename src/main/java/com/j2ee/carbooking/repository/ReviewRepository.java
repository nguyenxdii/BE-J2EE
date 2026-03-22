package com.j2ee.carbooking.repository;

import com.j2ee.carbooking.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByVehicleId(String vehicleId);
}
