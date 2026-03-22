package com.j2ee.carbooking.model;

import com.j2ee.carbooking.enums.VerifyStatus;
import lombok.Data;

// Không có @Document vì đây là object nhúng trong User, không phải collection riêng
@Data
public class Identity {

    private String cccdFront; // URL ảnh mặt trước CCCD trên Cloudinary

    private String cccdBack; // URL ảnh mặt sau CCCD trên Cloudinary

    private String drivingLicense; // URL ảnh GPLX trên Cloudinary

    private VerifyStatus verifyStatus = VerifyStatus.PENDING; // Mặc định PENDING khi upload

    private String rejectReason; // Lý do từ chối — hiển thị để user biết cần sửa gì
}