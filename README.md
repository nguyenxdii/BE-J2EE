# ShopCar Backend - Hệ thống API Đặt xe

Phần Backend của dự án ShopCar được xây dựng trên nền tảng Java Spring Boot, cung cấp hệ thống API mạnh mẽ cho việc quản lý xe, xác thực người dùng và xử lý giao dịch.

## Hướng dẫn khởi chạy

### Yêu cầu hệ thống

1. **Điều kiện tiên quyết**:
   - Java 17 hoặc cao hơn
   - MongoDB (Local hoặc Atlas) đang chạy tại `localhost:27017`

2. **Cấu hình môi trường**:
   - File mẫu:
     ```bash
     cp src/main/resources/application.properties.example src/main/resources/application.properties
     ```
   - Cấu hình MongoDB URI, JWT Secret, thông tin Cloudinary và cấu hình Mail Server trong file vừa tạo.

3. **Thực thi**:
   - **Windows CLI**: `.\mvnw.cmd spring-boot:run`
   - **IDE (IntelliJ/Eclipse)**: Chạy class chính `CarbookingApplication.java`.

4. **URL API cơ sở**: `http://localhost:8080/api`

## Công nghệ sử dụng

- **Framework**: Spring Boot 3.x (Java 17)
   - **Lưu trữ**: MongoDB (Spring Data MongoDB)
   - **Bảo mật**: Spring Security & JWT (Kiểm soát truy cập dựa trên vai trò - RBAC)
   - **Tiện ích**: Lombok, Validation API
   - **Khởi tạo dữ liệu**: Tự động tạo dữ liệu mẫu (Admin, User, Xe, Danh mục) trong lần chạy đầu tiên.

## Cấu trúc dự án

```text
com.j2ee.carbooking/
├── config/          # Cấu hình Security, CORS và MongoDB Auditing
├── controller/      # Các Endpoint API (Auth, Xe, Danh mục, Quản trị)
├── dto/             # Đối tượng chuyển đổi dữ liệu (Requests & Responses)
├── enums/           # Định nghĩa các trạng thái Xe, Đơn hàng và Vai trò người dùng
├── exception/       # Xử lý ngoại lệ tập trung và định nghĩa lỗi tùy chỉnh
├── model/           # Các thực thể dữ liệu lưu trữ trong MongoDB
├── repository/      # Lớp giao diện tương tác với cơ sở dữ liệu
├── security/        # Triển khai bộ lọc JWT và cấu hình bảo mật
├── service/         # Triển khai logic nghiệp vụ lõi
└── util/            # Các lớp tiện ích và Data Seeder cho môi trường demo
```

## Các tính năng chính

- **Hệ thống xác thực**: Quản lý đăng nhập và phiên làm việc bảo mật bằng JSON Web Token (JWT).
- **Kiểm soát truy cập**: Phân quyền nghiêm ngặt giữa khách hàng (USER) và quản trị viên (ADMIN).
- **Kiểm tra tự động (Auditing)**: Cơ chế tự động ghi lại thời gian tạo và cập nhật cho tất cả bản ghi dữ liệu.
- **Chia sẻ tài nguyên nguồn gốc chéo (CORS)**: Được cấu hình sẵn để giao tiếp an toàn với Frontend React.
- **Xử lý bất đồng bộ**: Tích hợp dịch vụ gửi mail (OTP, thông báo) sử dụng @Async của Spring.
