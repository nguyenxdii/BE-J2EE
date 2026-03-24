package com.j2ee.carbooking.model;

import com.j2ee.carbooking.enums.Role;
import com.j2ee.carbooking.enums.UserStatus;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data // Lombok — tự sinh getter, setter, toString, equals
@Document(collection = "users") // Map tới collection "users" trong MongoDB
public class User {

    @Id
    private String id; // MongoDB dùng String cho ObjectId

    private String fullName; // Họ tên hiển thị

    @Indexed(unique = true) // Đánh index unique, tránh trùng email
    private String email;

    private String password; // Đã hash BCrypt — null nếu đăng nhập Google

    private String phone; // Số điện thoại

    private String avatar; // URL ảnh đại diện trên Cloudinary

    private Role role = Role.USER; // Mặc định là USER khi đăng ký

    private UserStatus status = UserStatus.ACTIVE; // Mặc định ACTIVE

    private Double walletBalance = 0.0; // Số dư ví, khởi tạo = 0

    private String googleId; // ID từ Google OAuth, null nếu đăng ký email/pass

    private Identity identity; // Thông tin xác minh danh tính (nhúng object con)

    @CreatedDate // Spring tự gán khi tạo mới
    private LocalDateTime createdAt;

    @LastModifiedDate // Spring tự cập nhật mỗi khi save
    private LocalDateTime updatedAt;

   private Long balance = 5000000L; // Mặc định cho 5 triệu để test như Dino muốn
}
