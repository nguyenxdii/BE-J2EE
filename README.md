# Backend - ShopCar (API Dịch vụ đặt xe)

Phần Backend của dự án ShopCar được xây dựng trên nền tảng Java Spring Boot, cung cấp các API mạnh mẽ cho hệ thống quản lý xe và người dùng.

## 🚀 Hướng dẫn khởi chạy

1. **Yêu cầu hệ thống**:
   - Java 17+
   - MongoDB (Local hoặc Atlas) đang chạy tại `localhost:27017`.

2. **Cấu hình Environment**:
   - Sao chép file mẫu:
     ```bash
     cp src/main/resources/application.properties.example src/main/resources/application.properties
     ```
   - Chỉnh sửa URI MongoDB, JWT Secret, và các thông số Cloudinary/Mail trong file mới tạo.

3. **Khởi chạy ứng dụng**:
   - **Windows**: `.\mvnw.cmd spring-boot:run`
   - **IntelliJ**: Mở project và Run file `CarbookingApplication.java`.

4. **API Base URL**: `http://localhost:8080/api`

## 🛠 Công nghệ sử dụng
- **Framework**: Spring Boot 3.x (Java 17)
- **Database**: MongoDB (Spring Data MongoDB)
- **Bảo mật**: Spring Security & JWT (Cấu hình RBAC - Role Based Access Control)
- **Tiện ích**: Lombok, Validation API
- **Data Seeding**: Tự động khởi tạo dữ liệu mẫu (Admin, User, Cars, Categories) khi ứng dụng chạy lần đầu.

## 📁 Cấu trúc gói (Packages)
```text
com.j2ee.carbooking/
├── config/          # Cấu hình Security, CORS, MongoDB Auditing
├── controller/      # API Endpoints (Auth, Vehicle, Category, v.v.)
├── dto/             # Request/Response data objects (AuthRequest, RegisterRequest)
├── enums/           # Trạng thái xe, Role người dùng (ADMIN, USER)
├── model/           # Các thực thể dữ liệu (Entities) lưu trữ trong MongoDB
├── repository/      # Giao diện tương tác với cơ sở dữ liệu
├── security/        # JWT Filter, JwtUtils, UserDetails implementation
├── service/         # Xử lý logic nghiệp vụ (AuthService, UserService)
└── util/            # DataSeeder khởi tạo dữ liệu demo
```

## ✨ Điểm nổi bật
- **Hệ thống xác thực (Auth)**: Sử dụng JWT (JSON Web Token) để quản lý phiên đăng nhập an toàn.
- **Phân quyền người dùng**: Phân biệt quyền giữa khách hàng (USER) và người quản trị (ADMIN).
- **Cấu hình Auditor**: Tự động ghi nhận `createdAt` và `updatedAt` cho mọi bản ghi.
- **Tương tác linh hoạt**: Hỗ trợ CORS cho phép Frontend React giao tiếp an toàn.
