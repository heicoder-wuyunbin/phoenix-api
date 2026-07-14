package com.phoenix.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SmsUtil {

    private static final Logger logger = LoggerFactory.getLogger(SmsUtil.class);

    /**
     * TODO: 暂时没有短信接口，直接把短信打印到控制台，等后续有短信接口了，再实际接入
     * 发送短信验证码
     * @param mobile 手机号
     * @param code 验证码
     */
    public static void send(String mobile, String code) {
        logger.info("【短信验证码】发送到手机号: {}, 验证码: {}", mobile, code);
        System.out.println("【短信验证码】发送到手机号: " + mobile + ", 验证码: " + code);
    }

    /**
     * TODO: 暂时没有短信接口，直接把短信打印到控制台，等后续有短信接口了，再实际接入
     * 发送邮件验证链接
     * @param email 邮箱地址
     * @param url 验证链接
     */
    public static void sendMail(String email, String url) {
        logger.info("【邮件验证】发送到邮箱: {}, 验证链接: {}", email, url);
        System.out.println("【邮件验证】发送到邮箱: " + email + ", 验证链接: " + url);
    }
}