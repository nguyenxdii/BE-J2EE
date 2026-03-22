package com.j2ee.carbooking.dto.response;

import lombok.Data;

@Data
public class BuyDepositListingResponse {
    private String listingId;
    private String orderId;
    private String vehicleName;
    private Double paidAmount;       // sellingPrice B đã trả
    private String payUrl;           // null nếu thanh toán ví, có URL nếu Momo
    private String message;
}
