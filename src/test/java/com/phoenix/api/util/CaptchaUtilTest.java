package com.phoenix.api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CaptchaUtil 静态方法单元测试")
class CaptchaUtilTest {

    @Test
    @DisplayName("合法邮箱应返回 true")
    void isEmail_validEmail_returnsTrue() {
        assertTrue(CaptchaUtil.isEmail("test@example.com"));
        assertTrue(CaptchaUtil.isEmail("user.name@domain.org"));
        assertTrue(CaptchaUtil.isEmail("user+tag@gmail.com"));
    }

    @Test
    @DisplayName("非法邮箱应返回 false")
    void isEmail_invalidEmail_returnsFalse() {
        assertFalse(CaptchaUtil.isEmail("invalid"));
        assertFalse(CaptchaUtil.isEmail("@example.com"));
        assertFalse(CaptchaUtil.isEmail("user@"));
        assertFalse(CaptchaUtil.isEmail("user@.com"));
        assertFalse(CaptchaUtil.isEmail(null));
        assertFalse(CaptchaUtil.isEmail(""));
    }

    @Test
    @DisplayName("合法手机号应返回 true")
    void isMobile_validMobile_returnsTrue() {
        assertTrue(CaptchaUtil.isMobile("13800138000"));
        assertTrue(CaptchaUtil.isMobile("15912345678"));
        assertTrue(CaptchaUtil.isMobile("18600000000"));
    }

    @Test
    @DisplayName("非法手机号应返回 false")
    void isMobile_invalidMobile_returnsFalse() {
        assertFalse(CaptchaUtil.isMobile("12345678901"));
        assertFalse(CaptchaUtil.isMobile("1380013800"));
        assertFalse(CaptchaUtil.isMobile("138001380001"));
        assertFalse(CaptchaUtil.isMobile("abc"));
        assertFalse(CaptchaUtil.isMobile(null));
        assertFalse(CaptchaUtil.isMobile(""));
    }
}
