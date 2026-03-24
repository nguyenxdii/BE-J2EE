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
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Ảnh tải lên không hợp lệ");
        }
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", folder
                    )
            );
            return (String) result.get("secure_url");
        } catch (IOException ex) {
            throw new RuntimeException("Upload ảnh Cloudinary thất bại", ex);
        }
    }

    public void deleteByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        String publicId = extractPublicId(imageUrl);
        if (publicId == null) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException ex) {
            throw new RuntimeException("Xóa ảnh Cloudinary thất bại", ex);
        }
    }

    private String extractPublicId(String imageUrl) {
        try {
            int uploadIdx = imageUrl.indexOf("/upload/");
            if (uploadIdx < 0) {
                return null;
            }
            String path = imageUrl.substring(uploadIdx + "/upload/".length());
            if (path.startsWith("v")) {
                int slash = path.indexOf('/');
                if (slash > 0) {
                    path = path.substring(slash + 1);
                }
            }
            int dot = path.lastIndexOf('.');
            return dot > 0 ? path.substring(0, dot) : path;
        } catch (Exception ex) {
            return null;
        }
    }
}
