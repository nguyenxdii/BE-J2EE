package com.j2ee.carbooking.dto.response;

import com.j2ee.carbooking.enums.DepositListingStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DepositListingResponse {
    private String id;

    // Thông tin xe (lấy từ vehicles)
    private String vehicleId;
    private String vehicleName;
    private String vehicleImage;   // lấy từ images.get(0)
    private String vehicleBrand;

    // Thông tin đơn hàng gốc (lấy từ orders)
    private String orderId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;

    // Thông tin tài chính
    private Double originalDeposit;  // cọc gốc A đã đóng
    private Double sellingPrice;     // Giá B phải trả
    private Double savedAmount;      // Tiết kiệm được = originalDeposit - sellingPrice

    // Trạng thái & thời gian
    private DepositListingStatus status;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
}
