package com.word.wordlearning.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendVerificationCode(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("单词学习 - 验证码");
            helper.setText(buildHtmlContent(code), true);

            mailSender.send(message);
            log.info("验证码邮件已发送: {} -> {}", toEmail, code);
            return true;
        } catch (MessagingException e) {
            log.error("邮件发送失败: {} -> {}", toEmail, e.getMessage());
            return false;
        }
    }

    private String buildHtmlContent(String code) {
        return "<div style='max-width:480px;margin:0 auto;padding:32px;font-family:Arial,sans-serif;background:#f5f7fa;border-radius:12px'>"
                + "<h2 style='color:#3577ef;text-align:center'>单词学习</h2>"
                + "<p style='color:#333;font-size:15px;text-align:center'>您的验证码为：</p>"
                + "<div style='background:#3577ef;color:#fff;font-size:32px;font-weight:bold;text-align:center;padding:16px;border-radius:8px;letter-spacing:8px;margin:20px 0'>"
                + code
                + "</div>"
                + "<p style='color:#8899aa;font-size:13px;text-align:center'>验证码 5 分钟内有效，请勿透露给他人。</p>"
                + "</div>";
    }
}
