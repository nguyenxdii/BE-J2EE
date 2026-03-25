package com.j2ee.carbooking.dto.request;

import jakarta.validation.constraints.*;

public class RegisterRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
