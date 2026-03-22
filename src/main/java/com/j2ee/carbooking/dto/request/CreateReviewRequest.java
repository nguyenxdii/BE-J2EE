package com.j2ee.carbooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateReviewRequest {
    @NotBlank
    private String orderId;

    @NotBlank
    private String vehicleId;

    @Min(1) @Max(5)
    private Integer rating;

    private String comment;
}
