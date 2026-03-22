package com.j2ee.carbooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDepositListingRequest {
    @NotBlank
    private String orderId; // User chọn đơn nào muốn bán suất cọc
}
