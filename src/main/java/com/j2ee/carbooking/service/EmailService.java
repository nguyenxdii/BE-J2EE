package com.j2ee.carbooking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Gửi OTP đăng ký
    public void sendOtpRegister(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[ShopCar] Mã xác nhận đăng ký");
        message.setText(
            "Xin chào!\n\n"
            + "Mã OTP xác nhận đăng ký tài khoản ShopCar của bạn là:\n\n"
            + "  " + otp + "\n\n"
            + "Mã có hiệu lực trong 5 phút.\n"
            + "Nếu bạn không yêu cầu, hãy bỏ qua email này.\n\n"
            + "ShopCar Team"
        );
        mailSender.send(message);
    }

    // Gửi link reset mật khẩu
    public void sendResetPassword(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[ShopCar] Đặt lại mật khẩu");
        message.setText(
            "Xin chào!\n\n"
            + "Bạn đã yêu cầu đặt lại mật khẩu ShopCar.\n"
            + "Click vào link bên dưới để đặt lại mật khẩu:\n\n"
            + "  " + resetLink + "\n\n"
            + "Link có hiệu lực trong 15 phút.\n"
            + "Nếu bạn không yêu cầu, hãy bỏ qua email này.\n\n"
            + "ShopCar Team"
        );
        mailSender.send(message);
    }
}
