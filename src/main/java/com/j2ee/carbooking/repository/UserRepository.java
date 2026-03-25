package com.j2ee.carbooking.repository;

import com.j2ee.carbooking.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
    
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    long countByIdentityVerifyStatus(com.j2ee.carbooking.enums.VerifyStatus verifyStatus);
}
