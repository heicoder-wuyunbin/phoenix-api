package com.phoenix.api.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil 单元测试")
@SuppressWarnings("null")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "phoenix_jwt_secret_key_2026_phoenix_jwt_secret_key_2026");
        ReflectionTestUtils.setField(jwtUtil, "expire", 86400000L);
    }

    @Test
    @DisplayName("生成 Token 应返回非空字符串")
    void generateToken_validInput_returnsToken() {
        String token = jwtUtil.generateToken(1L, "testuser");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("解析 Token 应返回正确的 Claims")
    void parseToken_validToken_returnsClaims() {
        String token = jwtUtil.generateToken(1L, "testuser");
        Claims claims = jwtUtil.parseToken(token);
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
    }

    @Test
    @DisplayName("从 Token 获取 userId 应返回正确值")
    void getUserId_validToken_returnsUserId() {
        String token = jwtUtil.generateToken(100L, "testuser");
        Long userId = jwtUtil.getUserId(token);
        assertEquals(100L, userId);
    }

    @Test
    @DisplayName("从 Token 获取 username 应返回正确值")
    void getUsername_validToken_returnsUsername() {
        String token = jwtUtil.generateToken(1L, "admin");
        String username = jwtUtil.getUsername(token);
        assertEquals("admin", username);
    }

    @Test
    @DisplayName("未过期 Token 应返回 false")
    void isTokenExpired_validToken_returnsFalse() {
        String token = jwtUtil.generateToken(1L, "testuser");
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("已过期 Token 应返回 true")
    void isTokenExpired_expiredToken_returnsTrue() {
        ReflectionTestUtils.setField(jwtUtil, "expire", -1000L);
        String token = jwtUtil.generateToken(1L, "testuser");
        assertTrue(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("无效 Token 应返回 true")
    void isTokenExpired_invalidToken_returnsTrue() {
        assertTrue(jwtUtil.isTokenExpired("invalid.token.here"));
    }
}
