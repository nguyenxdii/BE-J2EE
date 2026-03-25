package com.j2ee.carbooking.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("shopcar.j2ee@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            // We don't throw exception to avoid blocking business flow
        }
    }

    public void sendVerificationEmail(String to, String fullName, boolean isApproved, String reason) {
        String subject = isApproved ? "Chúc mừng! Hồ sơ của bạn đã được duyệt" : "Thông báo kết quả xác minh hồ sơ";
        String body = isApproved 
            ? "Chào " + fullName + ",\n\nChúc mừng! Hồ sơ CCCD/GPLX của bạn đã được Admin duyệt thành công. Bây giờ bạn có thể đầy đủ trải nghiệm dịch vụ của ShopCar.\n\nTrân trọng,\nĐội ngũ ShopCar."
            : "Chào " + fullName + ",\n\nRất tiếc, hồ sơ xác minh của bạn không được duyệt.\nLý do: " + reason + "\nVui lòng cập nhật lại thông tin chính xác hơn.\n\nTrân trọng,\nĐội ngũ ShopCar.";
        
        sendEmail(to, subject, body);
    }

    public void sendOrderCancelEmail(String to, String orderCode, String reason) {
        String subject = "Thông báo hủy đơn hàng - " + orderCode;
        String body = "Chào bạn,\n\nĐơn hàng " + orderCode + " của bạn đã bị hủy bởi hệ thống.\nLý do: " + reason + "\nNếu bạn đã thanh toán, tiền cọc sẽ được hoàn lại vào ví.\n\nTrân trọng,\nĐội ngũ ShopCar.";
        
        sendEmail(to, subject, body);
    }
}
