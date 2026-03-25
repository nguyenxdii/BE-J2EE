package com.j2ee.carbooking.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "otpTokens")
public class OtpToken {

    @Id
    private String id;

    private String email;

    private String token; // 6 số cho OTP đăng ký, UUID cho reset password

    private String type;
    // "REGISTER" — xác minh OTP khi đăng ký
    // "RESET"    — link reset mật khẩu

    private LocalDateTime expiredAt; // OTP: 5 phút, Reset: 15 phút

    private Boolean used = false; // đã dùng rồi thì không dùng lại được
}
