package com.j2ee.carbooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateDepositListingRequest {
    @NotBlank
    private String orderId; // User chọn đơn nào muốn bán suất cọc

    @NotNull
    private Double sellingPrice; // Giá muốn bán (do user nhập, tối đa 60% tiền cọc)
}
