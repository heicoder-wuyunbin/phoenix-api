package com.phoenix.api.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class SmsUtil {

    private static final Logger logger = LoggerFactory.getLogger(SmsUtil.class);

    private final JavaMailSender mailSender;
    private final String mailFrom;

    public SmsUtil(JavaMailSender mailSender, @Value("${spring.mail.username}") String mailFrom) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
    }

    /**
     * 发送短信验证码（暂时打印到控制台，待接入短信接口）
     */
    public static void send(String mobile, String code) {
        logger.info("【短信验证码】发送到手机号: {}, 验证码: {}", mobile, code);
        System.out.println("【短信验证码】发送到手机号: " + mobile + ", 验证码: " + code);
    }

    /**
     * 发送邮箱验证邮件
     *
     * @param email 收件人邮箱
     * @param url   验证链接
     */
    public void sendMail(String email, String url) {
        String sendTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String subject = "邮箱验证";
        String text = buildEmailContent(url, sendTime);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(text, true);

            mailSender.send(message);
            logger.info("【邮件验证】发送成功 -> 收件人: {}", email);
        } catch (MailException | MessagingException e) {
            logger.error("【邮件验证】发送失败 -> 收件人: {}, 错误: {}", email, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建邮件 HTML 正文
     */
    private String buildEmailContent(String url, String sendTime) {
        return "<!DOCTYPE html>" +
                "<html lang=\"zh-CN\">" +
                "<head><meta charset=\"UTF-8\"></head>" +
                "<body style=\"font-family: 'Microsoft YaHei', Arial, sans-serif; background-color: #f5f5f5; padding: 20px;\">" +
                "<div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); padding: 30px;\">" +
                "<h2 style=\"color: #333333; text-align: center; margin-bottom: 20px;\">邮箱验证</h2>" +
                "<p style=\"color: #555555; font-size: 14px; line-height: 1.8;\">您好，您正在验证邮箱，请点击下方按钮完成验证：</p>" +
                "<div style=\"text-align: center; margin: 30px 0;\">" +
                "<a href=\"" + url + "\" style=\"display: inline-block; background-color: #1890ff; color: #ffffff; text-decoration: none; padding: 12px 36px; border-radius: 4px; font-size: 15px;\">验证邮箱</a>" +
                "</div>" +
                "<p style=\"color: #999999; font-size: 12px; text-align: center;\">或复制以下链接到浏览器：</p>" +
                "<p style=\"color: #1890ff; font-size: 12px; text-align: center; word-break: break-all;\">" + url + "</p>" +
                "<p style=\"color: #999999; font-size: 12px; text-align: center;\">如果您没有发起此操作，请忽略此邮件。</p>" +
                "<p style=\"color: #999999; font-size: 12px; text-align: center;\">发送时间：" + sendTime + "</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

}