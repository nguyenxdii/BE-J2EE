package com.j2ee.carbooking.repository;

import com.j2ee.carbooking.model.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    List<Vehicle> findByCategoryId(String categoryId);
}
