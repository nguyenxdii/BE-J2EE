package com.j2ee.carbooking.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Sử dụng @Value để lấy thông tin từ file application.properties
    public CloudinaryService(
            @Value("${app.cloudinary.cloud-name}") String cloudName,
            @Value("${app.cloudinary.api-key}") String apiKey,
            @Value("${app.cloudinary.api-secret}") String apiSecret) {
        
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        ));
    }

    public String uploadImage(MultipartFile file) throws IOException {
        try {
            // Upload file và ép kiểu Map để hết cảnh báo vàng
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            
            // Trả về URL của ảnh
            return uploadResult.get("url").toString();
        } catch (IOException e) {
            throw new IOException("Lỗi khi upload ảnh lên Cloudinary: " + e.getMessage());
        }
    }
}