package com.j2ee.carbooking.dto.request;

import com.j2ee.carbooking.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BuyDepositListingRequest {
    @NotBlank
    private String listingId;

    @NotNull
    private PaymentMethod paymentMethod;

    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
}
