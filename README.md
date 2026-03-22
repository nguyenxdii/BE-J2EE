# Backend - ShopCar (API Dịch vụ đặt xe)

Phần Backend của dự án ShopCar được xây dựng trên nền tảng Java Spring Boot, cung cấp các API mạnh mẽ cho hệ thống quản lý xe.

## 🚀 Hướng dẫn khởi chạy

1. **Yêu cầu hệ thống**:
   - Java 17+
   - MongoDB (Local hoặc Atlas) đang chạy tại `localhost:27017`.

2. **Cấu hình Environment**:
   - Sao chép file mẫu:
     ```bash
     cp src/main/resources/application.properties.example src/main/resources/application.properties
     ```
   - Chỉnh sửa URI MongoDB và các thông số cần thiết trong file mới tạo.

3. **Khởi chạy ứng dụng**:
   - **Windows**: `.\mvnw.cmd spring-boot:run`
   - **IntelliJ**: Mở project và Run file `CarbookingApplication.java`.

4. **API Base URL**: `http://localhost:8080/api`

## 🛠 Công nghệ sử dụng
- **Framework**: Spring Boot 3.x (Java 17)
- **Database**: MongoDB (Spring Data MongoDB)
- **Bảo mật**: Spring Security (CORS & API Authorization)
- **Tiện ích**: Lombok, MapStruct (nếu có), Validation API
- **Data Seeding**: Tự động khởi tạo dữ liệu mẫu khi ứng dụng khởi chạy.

## 📁 Cấu trúc gói (Packages)
```text
com.j2ee.carbooking/
├── config/          # Cấu hình Security, CORS, MongoDB Auditing
├── controller/      # Các lớp tiếp nhận và xử lý yêu cầu HTTP (REST Endpoints)
├── dto/             # Data Transfer Objects cho việc truyền gửi dữ liệu
├── enums/           # Các định nghĩa hằng số (Trạng thái xe, Role người dùng)
├── model/           # Các thực thể dữ liệu (Entities) lưu trữ trong MongoDB
├── repository/      # Giao diện tương tác với cơ sở dữ liệu
├── security/        # Triển khai các lớp bảo mật chi tiết
├── service/         # Xử lý logic nghiệp vụ chính của hệ thống
└── util/            # Các tiện ích bổ trợ (DataSeeder khởi tạo dữ liệu)
```

## ✨ Điểm nổi bật
- **Bảo mật**: Cấu hình Spring Security linh hoạt, quản lý quyền truy cập API.
- **Tự động hóa**: Sử dụng `@EnableMongoAuditing` để hỗ trợ ghi nhận thời gian (CreatedDate, LastModifiedDate).
- **Dữ liệu**: Cơ chế Data Seeding giúp xây dựng môi trường demo nhanh chóng chỉ sau một lần chạy.
