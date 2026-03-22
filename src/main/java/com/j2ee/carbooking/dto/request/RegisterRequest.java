package com.j2ee.carbooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;
}
