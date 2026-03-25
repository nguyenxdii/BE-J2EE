package com.j2ee.carbooking.service;

import com.j2ee.carbooking.dto.request.UpdateProfileRequest;
import com.j2ee.carbooking.model.Identity;
import com.j2ee.carbooking.model.User;
import com.j2ee.carbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    // Lấy thông tin cá nhân
    public User getProfile(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    // Cập nhật thông tin cá nhân
    public User updateProfile(String userId, UpdateProfileRequest request, MultipartFile avatar) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());

        if (avatar != null && !avatar.isEmpty()) {
            String url = cloudinaryService.uploadImage(avatar, "avatars");
            user.setAvatar(url);
        }

        return userRepository.save(user);
    }

    // Upload CCCD / GPLX
    public User uploadIdentity(String userId, MultipartFile cccdFront, MultipartFile cccdBack, MultipartFile drivingLicense) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Identity identity = user.getIdentity() == null ? new Identity() : user.getIdentity();

        if (cccdFront != null && !cccdFront.isEmpty()) {
            identity.setCccdFront(cloudinaryService.uploadImage(cccdFront, "kyc"));
        }
        if (cccdBack != null && !cccdBack.isEmpty()) {
            identity.setCccdBack(cloudinaryService.uploadImage(cccdBack, "kyc"));
        }
        if (drivingLicense != null && !drivingLicense.isEmpty()) {
            identity.setDrivingLicense(cloudinaryService.uploadImage(drivingLicense, "kyc"));
        }

        user.setIdentity(identity);
        return userRepository.save(user);
    }
}
