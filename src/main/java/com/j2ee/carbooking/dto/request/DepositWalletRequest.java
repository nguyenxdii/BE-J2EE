package com.j2ee.carbooking.dto.request;

import com.j2ee.carbooking.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DepositWalletRequest {
    @Min(value = 50000, message = "Nạp tối thiểu 50.000đ")
    private Double amount;

    @NotNull
    private PaymentMethod paymentMethod; // VNPAY hoặc MOMO
}
