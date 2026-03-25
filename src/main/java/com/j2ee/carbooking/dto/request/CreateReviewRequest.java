package com.j2ee.carbooking.dto.request;

import jakarta.validation.constraints.*;

public class CreateReviewRequest {
    @NotBlank
    private String orderId;

    @NotBlank
    private String vehicleId;

    @Min(1) @Max(5)
    private Integer rating;

    private String comment;

    public CreateReviewRequest() {}

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
