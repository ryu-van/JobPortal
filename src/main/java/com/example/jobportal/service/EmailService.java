package com.example.jobportal.service;

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


    @Async("taskExecutor")
    public void sendVerificationEmail(String toEmail, String fullName, String code) {
        try {
            log.info("📨 Sending verification code {} to {}", code, toEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực tài khoản của bạn");

            String htmlContent = buildVerificationCodeEmail(fullName, code);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("✅ Verification code sent successfully to {}", toEmail);

        } catch (MessagingException e) {
            log.error("❌ Failed to send verification code to {}: {}", toEmail, e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Unexpected error while sending email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    private String buildVerificationCodeEmail(String username, String code) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; border-radius: 8px; background-color: #f9f9f9; }
                    .code-box { background-color: #4CAF50; color: white; padding: 15px; font-size: 24px; text-align: center; border-radius: 6px; margin: 20px 0; }
                    .footer { margin-top: 30px; font-size: 12px; color: #666; text-align: center; }
                  </style>
                </head>
                <body>
                  <div class="container">
                    <h2>Xin chào %s!</h2>
                    <p>Cảm ơn bạn đã đăng ký tài khoản. Đây là mã xác thực của bạn:</p>
                    <div class="code-box">%s</div>
                    <p>Mã này có hiệu lực trong <strong>10 phút</strong>. Vui lòng không chia sẻ mã này với bất kỳ ai.</p>
                    <div class="footer">
                      <p>Nếu bạn không yêu cầu xác thực, vui lòng bỏ qua email này.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(username, code);
    }
}
