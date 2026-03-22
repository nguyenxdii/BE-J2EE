package com.j2ee.carbooking.dto.request;

import com.j2ee.carbooking.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateOrderRequest {
    @NotBlank
    private String vehicleId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private PaymentMethod paymentMethod;

    private String note;
}
