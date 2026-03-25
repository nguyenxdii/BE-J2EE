package com.j2ee.carbooking.dto.request;

import com.j2ee.carbooking.enums.PaymentMethod;
import jakarta.validation.constraints.*;

public class DepositWalletRequest {
    @Min(value = 50000, message = "Nạp tối thiểu 50.000đ")
    private Double amount;

    @NotNull
    private PaymentMethod paymentMethod; // VNPAY hoặc MOMO

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
