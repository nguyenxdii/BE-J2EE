package com.j2ee.carbooking.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folder) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "carbooking/" + folder));
            return uploadResult.get("url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload ảnh lên Cloudinary: " + e.getMessage());
        }
    }

    public void deleteByUrl(String url) {
        if (url == null || !url.contains("carbooking/")) return;
        try {
            // Parse public_id từ URL
            // Ví dụ: http://res.cloudinary.com/.../carbooking/avatars/xyz123.jpg -> carbooking/avatars/xyz123
            String publicId = url.substring(url.lastIndexOf("carbooking/"), url.lastIndexOf("."));
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            System.err.println("Lỗi xóa ảnh trên Cloudinary: " + e.getMessage());
        }
    }
}
