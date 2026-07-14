package com.phoenix.api.integration;

import com.phoenix.api.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("验证码模块集成测试")
class CaptchaIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("获取验证码 - 无 key 参数自动生成")
    void getCaptcha_withoutKey_generatesNewCaptcha() throws Exception {
        mockMvc.perform(get("/api/user/captcha"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"));
    }

    @Test
    @DisplayName("获取验证码 - 指定 key 参数")
    void getCaptcha_withKey_generatesCaptcha() throws Exception {
        String key = "test-captcha-key";

        mockMvc.perform(get("/api/user/captcha")
                        .param("key", key))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"));

        // 验证 Redis 中存储了验证码
        String storedCaptcha = redisTemplate.opsForValue().get("captcha:" + key);
        assertNotNull(storedCaptcha);
        assertEquals(4, storedCaptcha.length());
    }

    @Test
    @DisplayName("验证码存储到 Redis - 5分钟过期")
    void captcha_storedInRedis_withExpiration() throws Exception {
        String key = "expiration-test-key";

        mockMvc.perform(get("/api/user/captcha")
                        .param("key", key))
                .andExpect(status().isOk());

        // 验证验证码存在
        String storedCaptcha = redisTemplate.opsForValue().get("captcha:" + key);
        assertNotNull(storedCaptcha);

        // 验证过期时间（大约5分钟）
        Long expireTime = redisTemplate.getExpire("captcha:" + key);
        assertNotNull(expireTime);
        assertTrue(expireTime > 0 && expireTime <= 300);
    }
}
