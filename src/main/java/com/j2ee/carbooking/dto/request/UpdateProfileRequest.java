package com.j2ee.carbooking.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phone;
}
