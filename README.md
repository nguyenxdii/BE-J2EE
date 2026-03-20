# Backend - Car Booking

Phần Backend của dự án Car Booking được xây dựng bằng Spring Boot và MongoDB.

## 🚀 Hướng dẫn khởi chạy

1. Đảm bảo **MongoDB** đang chạy tại `localhost:27017`.
2. Chạy ứng dụng bằng Maven Wrapper:
   - **Windows (PowerShell)**: `.\mvnw.cmd spring-boot:run`
   - **Linux/macOS**: `./mvnw spring-boot:run`
3. **Sử dụng IntelliJ IDEA**:
   - Mở thư mục `backend` bằng IntelliJ.
   - Chờ Maven tải xong các dependency.
   - Tìm file `CarbookingApplication.java` (trong `src/main/java/...`), chuột phải và chọn **Run 'CarbookingApplication'**.
4. API sẽ chạy tại: `http://localhost:8080`

## ⚙️ Cấu hình Database & Environment
Dự án sử dụng cơ chế bảo mật thông tin cấu hình qua file `application.properties`. 

**Các bước thiết lập:**
1. Sao chép file mẫu:
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```
2. Mở file `src/main/resources/application.properties` và chỉnh sửa các thông số (URI MongoDB, port, v.v.) cho phù hợp với máy của bạn.

> [!IMPORTANT]
> File `application.properties` đã được liệt kê trong `.gitignore` để tránh rò rỉ thông tin cá nhân. Đừng bao giờ commit file này lên Git.

## 🛠 Công nghệ sử dụng

- **Framework**: Spring Boot 3.5.12
- **Database**: MongoDB (Spring Data MongoDB)
- **Security**: Spring Security
- **Utilities**: Lombok, Validation
