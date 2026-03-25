package com.j2ee.carbooking.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "categories")
public class Category {

    @Id
    private String id;

    private String name; // Tên danh mục: Xe số, Xe ga, Xe côn tay, Xe điện

    private String description; // Mô tả ngắn hiển thị trên trang chủ

    private String image; // URL icon danh mục trên Cloudinary
    
    private boolean hidden = false;

    @CreatedDate
    private LocalDateTime createdAt;
}
