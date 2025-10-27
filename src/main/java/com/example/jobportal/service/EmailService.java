package com.example.jobportal.service;

import com.example.jobportal.model.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Async("taskExecutor")
    public void sendVerificationEmail(User user, String token) {
        try {
            log.info("Starting to send verification email to: {}", user.getEmail());
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Xác nhận địa chỉ email của bạn");
            String verificationUrl = frontendUrl + "/verify-email?token=" + token;
            String htmlContent = buildVerificationEmail(user.getFullName(), verificationUrl);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("✅ Verification email sent successfully to: {}", user.getEmail());

        } catch (MessagingException msgEx) {
            log.error("❌ Failed to send verification email to: {}", user.getEmail(), msgEx);
        } catch (Exception e) {
            log.error("❌ An unexpected error occurred while sending verification email to: {}", user.getEmail(), e);
        }
    }

    private String buildVerificationEmail(String username, String verificationUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".button { background-color: #4CAF50; color: white; padding: 14px 20px; " +
                "text-decoration: none; border-radius: 4px; display: inline-block; margin: 20px 0; }" +
                ".footer { margin-top: 30px; font-size: 12px; color: #666; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<h2>Xin chào " + username + "!</h2>" +
                "<p>Cảm ơn bạn đã đăng ký tài khoản.</p>" +
                "<p>Vui lòng nhấn vào nút bên dưới để xác nhận email của bạn:</p>" +
                "<a href='" + verificationUrl + "' class='button'>Xác nhận Email</a>" +
                "<p>Hoặc copy link sau vào trình duyệt:</p>" +
                "<p>" + verificationUrl + "</p>" +
                "<p>Link này sẽ hết hạn sau 24 giờ.</p>" +
                "<div class='footer'>" +
                "<p>Nếu bạn không tạo tài khoản này, vui lòng bỏ qua email này.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }


}
