package com.j2ee.carbooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    private String oldPassword;

    @NotBlank @Size(min = 6, message = "Mật khẩu mới tối thiểu 6 ký tự")
    private String newPassword;
}
