package com.j2ee.carbooking.model;

import lombok.Data;

// Object nhúng trong Vehicle — không có @Document
@Data
public class Specs {

    private String engine; // Dung tích động cơ: 110cc, 150cc, 170cc

    private String fuelType; // Loại nhiên liệu: Xăng / Điện

    private String transmission; // Kiểu hộp số: Tay ga / Số / Côn tay
}