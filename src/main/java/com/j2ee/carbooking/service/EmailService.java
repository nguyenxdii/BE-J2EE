package com.j2ee.carbooking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Cơ chế gửi mail chung (tất cả đều Async)
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error sending email to " + to + ": " + e.getMessage());
        }
    }

    // Gửi OTP đăng ký
    @Async
    public void sendOtpRegister(String toEmail, String otp) {
        String subject = "[ShopCar] Mã xác nhận đăng ký";
        String text = "Xin chào!\n\n"
                    + "Mã OTP xác thực đăng ký tài khoản ShopCar của bạn là: " + otp + "\n\n"
                    + "Mã có hiệu lực trong 5 phút.\nNếu bạn không yêu cầu, hãy bỏ qua email này.\n\n"
                    + "ShopCar Team";
        sendEmail(toEmail, subject, text);
    }

    // Gửi link reset mật khẩu
    @Async
    public void sendResetPassword(String toEmail, String resetLink) {
        String subject = "[ShopCar] Đặt lại mật khẩu";
        String text = "Xin chào!\n\n"
                    + "Bạn đã yêu cầu đặt lại mật khẩu ShopCar.\n"
                    + "Click vào link bên dưới để đặt lại mật khẩu:\n\n"
                    + "  " + resetLink + "\n\n"
                    + "Link có hiệu lực trong 15 phút.\n"
                    + "Nếu bạn không yêu cầu, hãy bỏ qua email này.\n\n"
                    + "ShopCar Team";
        sendEmail(toEmail, subject, text);
    }

    @Async
    public void sendVerificationEmail(String to, String fullName, boolean isApproved, String reason) {
        String subject = isApproved ? "Chúc mừng! Hồ sơ của bạn đã được duyệt" : "Thông báo kết quả xác minh hồ sơ";
        String body = isApproved 
            ? "Chào " + fullName + ",\n\nChúc mừng! Hồ sơ CCCD/GPLX của bạn đã được Admin duyệt thành công. Bây giờ bạn có thể đầy đủ trải nghiệm dịch vụ của ShopCar.\n\nTrân trọng,\nĐội ngũ ShopCar."
            : "Chào " + fullName + ",\n\nRất tiếc, hồ sơ xác minh của bạn không được duyệt.\nLý do: " + reason + "\nVui lòng cập nhật lại thông tin chính xác hơn.\n\nTrân trọng,\nĐội ngũ ShopCar.";
        
        sendEmail(to, subject, body);
    }

    @Async
    public void sendOrderCancelEmail(String to, String orderCode, String reason) {
        String subject = "Thông báo hủy đơn hàng - " + orderCode;
        String body = "Chào bạn,\n\nĐơn hàng " + orderCode + " của bạn đã bị hủy bởi hệ thống.\nLý do: " + reason + "\nNếu bạn đã thanh toán, tiền cọc sẽ được hoàn lại vào ví.\n\nTrân trọng,\nĐội ngũ ShopCar.";
        
        sendEmail(to, subject, body);
    }
}
