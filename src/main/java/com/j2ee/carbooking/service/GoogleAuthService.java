package com.j2ee.carbooking.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.j2ee.carbooking.dto.response.AuthResponse;
import com.j2ee.carbooking.enums.Role;
import com.j2ee.carbooking.enums.UserStatus;
import com.j2ee.carbooking.model.User;
import com.j2ee.carbooking.repository.UserRepository;
import com.j2ee.carbooking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.google.client-id:your-google-client-id}")
    private String clientId;

    public AuthResponse loginWithGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new RuntimeException("ID Token không hợp lệ");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            // Kiểm tra user tồn tại chưa
            Optional<User> userOpt = userRepository.findByEmail(email);
            User user;

            if (userOpt.isPresent()) {
                user = userOpt.get();
                if (user.getStatus() == UserStatus.LOCKED) {
                    throw new RuntimeException("Tài khoản đã bị khoá");
                }
            } else {
                // Tạo user mới nếu chưa có
                user = new User();
                user.setEmail(email);
                user.setFullName(name);
                user.setAvatar(pictureUrl);
                user.setRole(Role.USER);
                user.setStatus(UserStatus.ACTIVE);
                user.setWalletBalance(0.0);
                userRepository.save(user);
            }

            String token = jwtUtil.generateToken(user.getId());
            return new AuthResponse(token, user);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi xác thực Google: " + e.getMessage());
        }
    }
}
