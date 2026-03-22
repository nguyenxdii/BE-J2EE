package com.j2ee.carbooking.dto.request;

import com.j2ee.carbooking.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BuyDepositListingRequest {
    @NotBlank
    private String listingId;

    @NotNull
    private PaymentMethod paymentMethod; // WALLET hoặc MOMO
}
