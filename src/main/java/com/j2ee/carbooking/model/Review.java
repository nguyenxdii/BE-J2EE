package com.j2ee.carbooking.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "reviews")
public class Review {

    @Id
    private String id;

    private String userId; // FK → users._id — người viết đánh giá

    private String vehicleId;
    // FK → vehicles._id
    // Lưu riêng để query danh sách review theo xe nhanh
    // mà không cần join qua orders

    private String orderId;
    // FK → orders._id
    // Đánh index unique trên field này
    // để đảm bảo mỗi đơn chỉ được review đúng 1 lần

    private Integer rating; // Số sao: 1 đến 5

    private String comment; // Nội dung nhận xét

    @CreatedDate
    private LocalDateTime createdAt;
    // Không có updatedAt — review không cho sửa sau khi đã đăng
}