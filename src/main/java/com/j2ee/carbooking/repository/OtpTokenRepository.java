package com.j2ee.carbooking.repository;

import com.j2ee.carbooking.model.OtpToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OtpTokenRepository extends MongoRepository<OtpToken, String> {
    Optional<OtpToken> findByEmailAndTokenAndType(String email, String token, String type);

    // Xoá OTP cũ khi tạo mới — tránh rác trong DB
    void deleteByEmailAndType(String email, String type);
}
