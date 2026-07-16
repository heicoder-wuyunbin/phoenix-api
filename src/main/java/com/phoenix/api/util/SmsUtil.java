package com.phoenix.api.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class SmsUtil {

    private static final Logger logger = LoggerFactory.getLogger(SmsUtil.class);

    private static final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final String UPPER_ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JavaMailSender mailSender;

    public SmsUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
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
        String verificationCode = generateVerificationCode();
        String identificationCode = generateIdentificationCode();
        String sendTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String subject = "登录验证码";
        String text = buildEmailContent(verificationCode, identificationCode, url, sendTime);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(text, true);

            mailSender.send(message);
            logger.info("【邮件验证】发送成功 -> 收件人: {}, 验证码: {}, 识别码: {}", email, verificationCode, identificationCode);
        } catch (MailException | MessagingException e) {
            logger.error("【邮件验证】发送失败 -> 收件人: {}, 错误: {}", email, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建邮件 HTML 正文
     */
    private String buildEmailContent(String verificationCode, String identificationCode, String url, String sendTime) {
        return "<!DOCTYPE html>" +
                "<html lang=\"zh-CN\">" +
                "<head><meta charset=\"UTF-8\"></head>" +
                "<body style=\"font-family: 'Microsoft YaHei', Arial, sans-serif; background-color: #f5f5f5; padding: 20px;\">" +
                "<div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); padding: 30px;\">" +
                "<h2 style=\"color: #333333; text-align: center; margin-bottom: 20px;\">登录验证码</h2>" +
                "<p style=\"color: #555555; font-size: 14px; line-height: 1.8;\">您好，您正在验证邮箱配置，以下是您的验证码：</p>" +
                "<div style=\"text-align: center; margin: 25px 0;\">" +
                "<span style=\"display: inline-block; font-size: 28px; font-weight: bold; color: #1890ff; letter-spacing: 4px; background-color: #f0f5ff; padding: 10px 25px; border-radius: 4px; border: 1px dashed #91d5ff;\">" + verificationCode + "</span>" +
                "</div>" +
                "<div style=\"text-align: center; margin: 15px 0;\">" +
                "<span style=\"font-size: 14px; color: #999999;\">识别码：</span>" +
                "<span style=\"font-size: 18px; font-weight: bold; color: #ff4d4f; letter-spacing: 2px;\">" + identificationCode + "</span>" +
                "</div>" +
                "<p style=\"color: #888888; font-size: 13px; text-align: center;\">请核对识别码再输入验证码，有效期 10 分钟。</p>" +
                "<div style=\"text-align: center; margin: 30px 0;\">" +
                "<a href=\"" + url + "\" style=\"display: inline-block; background-color: #1890ff; color: #ffffff; text-decoration: none; padding: 12px 36px; border-radius: 4px; font-size: 15px;\">验证邮箱</a>" +
                "</div>" +
                "<p style=\"color: #999999; font-size: 12px; text-align: center;\">如果您没有发起此操作，请忽略此邮件。</p>" +
                "<p style=\"color: #999999; font-size: 12px; text-align: center;\">发送时间：" + sendTime + "</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * 生成 6 位验证码（字母+数字）
     */
    private String generateVerificationCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    /**
     * 生成 3 位识别码（大写字母+数字）
     */
    private String generateIdentificationCode() {
        StringBuilder sb = new StringBuilder(3);
        for (int i = 0; i < 3; i++) {
            sb.append(UPPER_ALPHANUMERIC.charAt(RANDOM.nextInt(UPPER_ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
}