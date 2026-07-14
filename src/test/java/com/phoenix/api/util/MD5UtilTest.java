package com.phoenix.api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MD5Util 单元测试")
class MD5UtilTest {

    @Test
    @DisplayName("正常字符串应返回正确的 MD5 值")
    void encode_normalString_returnsMd5Hash() {
        String input = "hello";
        String result = MD5Util.encode(input);
        assertNotNull(result);
        assertEquals("5d41402abc4b2a76b9719d911017c592", result);
    }

    @Test
    @DisplayName("空字符串应返回空字符串的 MD5 值")
    void encode_emptyString_returnsMd5Hash() {
        String result = MD5Util.encode("");
        assertNotNull(result);
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", result);
    }

    @Test
    @DisplayName("null 输入应返回 null")
    void encode_nullInput_returnsNull() {
        String result = MD5Util.encode(null);
        assertNull(result);
    }

    @Test
    @DisplayName("中文字符串应返回正确的 MD5 值")
    void encode_chineseString_returnsMd5Hash() {
        String input = "你好";
        String result = MD5Util.encode(input);
        assertNotNull(result);
        assertEquals("7eca689f0d3389d9dea66ae112e5cfd7", result);
    }

    @Test
    @DisplayName("相同输入应返回相同 MD5 值")
    void encode_sameInput_returnsSameHash() {
        String input = "password123";
        String result1 = MD5Util.encode(input);
        String result2 = MD5Util.encode(input);
        assertEquals(result1, result2);
    }

    @Test
    @DisplayName("不同输入应返回不同 MD5 值")
    void encode_differentInput_returnsDifferentHash() {
        String result1 = MD5Util.encode("password1");
        String result2 = MD5Util.encode("password2");
        assertNotEquals(result1, result2);
    }
}
