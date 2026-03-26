package com.example.English.teaching.center.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String email, String fullName, String verificationCode) throws MessagingException{
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("Xác nhận tài khoản - Trung taam ECE");

        String verifyURL = baseUrl + "/verify?code=" + verificationCode;

        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                + "<h2 style='color: #00c8ff; text-align: center;'>Chào mừng đến với ECE!</h2>"
                + "<p>Chào <b>" + fullName + "</b>,</p>"
                + "<p>Cảm ơn bạn đã đăng ký tài khoản. Vui lòng bấm vào nút bên dưới để xác nhận địa chỉ email và kích hoạt tài khoản của bạn:</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<a href='" + verifyURL + "' style='background-color: #00c8ff; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Xác Nhận Email</a>"
                + "</div>"
                + "<p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>"
                + "<hr style='border: none; border-top: 1px solid #eee;'/>"
                + "<p style='font-size: 0.8em; color: #888; text-align: center;'>TT Ngoại ngữ ECE - English Center for Everyone</p>"
                + "</div>";

        helper.setText(content, true);
        mailSender.send(message);
    }
}
