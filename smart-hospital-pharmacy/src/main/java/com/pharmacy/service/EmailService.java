package com.pharmacy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private static final String STAFF_NAME_TOKEN = "{{STAFF_NAME}}";
    private static final String OTP_TOKEN = "{{OTP}}";
    private static final String EMAIL_TOKEN = "{{EMAIL}}";
    private static final String PASSWORD_TOKEN = "{{PASSWORD}}";

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp, String staffName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(Objects.requireNonNull(fromEmail, "fromEmail must not be null"));
            helper.setTo(Objects.requireNonNull(toEmail, "toEmail must not be null"));
            helper.setSubject("Smart Hospital Pharmacy - Password Reset OTP");

            String htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <style>
                            body { font-family: 'Segoe UI', Arial, sans-serif; background: #f4f6f9; margin: 0; padding: 0; }
                            .container { max-width: 600px; margin: 40px auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                            .header { background: linear-gradient(135deg, #1a3a5c 0%, #0d7c66 100%); padding: 30px; text-align: center; }
                            .header h1 { color: white; margin: 0; font-size: 22px; font-weight: 700; }
                            .header p { color: rgba(255,255,255,0.85); margin: 5px 0 0; font-size: 14px; }
                            .body { padding: 40px 30px; }
                            .greeting { font-size: 16px; color: #333; margin-bottom: 20px; }
                            .otp-box { background: linear-gradient(135deg, #f0f9f6, #e8f4fd); border: 2px dashed #0d7c66; border-radius: 12px; padding: 25px; text-align: center; margin: 25px 0; }
                            .otp-label { font-size: 13px; color: #666; text-transform: uppercase; letter-spacing: 2px; margin-bottom: 10px; }
                            .otp-code { font-size: 42px; font-weight: 900; color: #1a3a5c; letter-spacing: 10px; font-family: 'Courier New', monospace; }
                            .otp-validity { font-size: 13px; color: #e74c3c; margin-top: 10px; font-weight: 600; }
                            .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 12px 16px; border-radius: 6px; font-size: 13px; color: #856404; margin: 20px 0; }
                            .footer { background: #f8f9fa; padding: 20px 30px; text-align: center; font-size: 12px; color: #999; border-top: 1px solid #eee; }
                            .hospital-icon { font-size: 36px; margin-bottom: 8px; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <div class="hospital-icon">🏥</div>
                                <h1>Smart Hospital Pharmacy</h1>
                                <p>Password Reset Request</p>
                            </div>
                            <div class="body">
                                <p class="greeting">Dear <strong>{{STAFF_NAME}}</strong>,</p>
                                <p>We received a request to reset your password for the Smart Hospital Pharmacy System. Use the OTP below to complete your password reset:</p>
                                <div class="otp-box">
                                    <div class="otp-label">Your One-Time Password</div>
                                    <div class="otp-code">{{OTP}}</div>
                                    <div class="otp-validity">⏱ Valid for 10 minutes only</div>
                                </div>
                                <div class="warning">
                                    ⚠️ <strong>Security Notice:</strong> Never share this OTP with anyone. Our team will never ask for your OTP. If you did not request this, please contact your administrator immediately.
                                </div>
                                <p style="color: #666; font-size: 14px;">If you did not request a password reset, please ignore this email. Your password will remain unchanged.</p>
                            </div>
                            <div class="footer">
                                <p>© 2024 Smart Hospital Pharmacy System. All rights reserved.</p>
                                <p>This is an automated message. Please do not reply to this email.</p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """
                    .replace(STAFF_NAME_TOKEN, staffName)
                    .replace(OTP_TOKEN, otp);

            helper.setText(Objects.requireNonNull(htmlContent, "htmlContent must not be null"), true);
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

    public void sendAccountLockedEmail(String toEmail, String staffName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(Objects.requireNonNull(fromEmail, "fromEmail must not be null"));
            helper.setTo(Objects.requireNonNull(toEmail, "toEmail must not be null"));
            helper.setSubject("Smart Hospital Pharmacy - Account Locked");
            String html = """
                    <html><body style="font-family:Arial,sans-serif;background:#f4f6f9;padding:20px;">
                    <div style="max-width:500px;margin:auto;background:white;border-radius:10px;padding:30px;box-shadow:0 2px 10px rgba(0,0,0,0.1);">
                    <h2 style="color:#e74c3c;">⚠️ Account Locked</h2>
                    <p>Dear <strong>{{STAFF_NAME}}</strong>,</p>
                    <p>Your account has been <strong>locked</strong> due to multiple failed login attempts.</p>
                    <p>Please contact your administrator to unlock your account.</p>
                    <hr style="border:none;border-top:1px solid #eee;margin:20px 0;">
                    <p style="color:#999;font-size:12px;">Smart Hospital Pharmacy System</p>
                    </div></body></html>
                    """
                    .replace(STAFF_NAME_TOKEN, staffName);
            helper.setText(Objects.requireNonNull(html, "html must not be null"), true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send account locked email: {}", e.getMessage());
        }
    }

    public void sendWelcomeEmail(String toEmail, String staffName, String tempPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(Objects.requireNonNull(fromEmail, "fromEmail must not be null"));
            helper.setTo(Objects.requireNonNull(toEmail, "toEmail must not be null"));
            helper.setSubject("Welcome to Smart Hospital Pharmacy System");
            String html = """
                    <html><body style="font-family:Arial,sans-serif;background:#f4f6f9;padding:20px;">
                    <div style="max-width:500px;margin:auto;background:white;border-radius:10px;padding:30px;">
                    <h2 style="color:#1a3a5c;">🏥 Welcome to Smart Hospital Pharmacy!</h2>
                    <p>Dear <strong>{{STAFF_NAME}}</strong>,</p>
                    <p>Your account has been created. Here are your login credentials:</p>
                    <div style="background:#f0f9f6;border-radius:8px;padding:15px;margin:15px 0;">
                        <p><strong>Email:</strong> {{EMAIL}}</p>
                        <p><strong>Password:</strong> {{PASSWORD}}</p>
                    </div>
                    <p style="color:#e74c3c;">Please change your password after first login.</p>
                    </div></body></html>
                    """
                    .replace(STAFF_NAME_TOKEN, staffName)
                    .replace(EMAIL_TOKEN, toEmail)
                    .replace(PASSWORD_TOKEN, tempPassword);
            helper.setText(Objects.requireNonNull(html, "html must not be null"), true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send welcome email: {}", e.getMessage());
        }
    }
}
