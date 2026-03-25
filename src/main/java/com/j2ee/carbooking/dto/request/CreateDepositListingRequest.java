package com.j2ee.carbooking.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CreateDepositListingRequest {
    @NotBlank
    private String orderId;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
}
