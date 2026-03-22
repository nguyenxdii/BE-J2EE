package com.j2ee.carbooking.model;

import com.j2ee.carbooking.enums.VehicleStatus;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "vehicles")
public class Vehicle {

    @Id
    private String id;

    private String name; // Tên xe: "Honda Wave Alpha 2022"

    private String categoryId; // FK tới categories._id

    private String brand; // Hãng xe: Honda, Yamaha, Suzuki, VinFast

    private String model; // Model: Wave, Exciter, Airblade

    private Integer year; // Năm sản xuất

    @Indexed(unique = true)
    private String licensePlate; // Biển số xe — unique

    private Double pricePerDay; // Giá thuê theo ngày (VNĐ)

    private Double depositAmount; // Tiền cọc khi đặt xe — đây là số A sẽ mất nếu không đến

    private String description; // Mô tả chi tiết hiển thị trang chi tiết xe

    private List<String> images; // Danh sách URL ảnh Cloudinary, ảnh [0] là ảnh đại diện

    private Specs specs; // Thông số kỹ thuật (nhúng object con)

    private VehicleStatus status = VehicleStatus.AVAILABLE; // Mặc định AVAILABLE

    private Double avgRating = 0.0; // Điểm trung bình từ reviews — cập nhật mỗi khi có review mới

    private Integer totalReviews = 0; // Tổng số lượt đánh giá

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}