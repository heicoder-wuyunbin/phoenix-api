package com.phoenix.api.controller;

import com.phoenix.api.exception.GlobalExceptionHandler;
import com.phoenix.api.util.CaptchaUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CaptchaController 单元测试")
class CaptchaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CaptchaUtil captchaUtil;

    @InjectMocks
    private CaptchaController captchaController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(captchaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/user/captcha - 无 key 参数时生成验证码")
    void getCaptcha_noKey_returnsImage() throws Exception {
        byte[] imageBytes = new byte[]{1, 2, 3, 4};
        when(captchaUtil.generateCaptcha(anyString())).thenReturn(imageBytes);

        mockMvc.perform(get("/api/user/captcha"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"));
    }

    @Test
    @DisplayName("GET /api/user/captcha - 有 key 参数时生成验证码")
    void getCaptcha_withKey_returnsImage() throws Exception {
        byte[] imageBytes = new byte[]{5, 6, 7, 8};
        when(captchaUtil.generateCaptcha("test-key")).thenReturn(imageBytes);

        mockMvc.perform(get("/api/user/captcha")
                        .param("key", "test-key"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"));
    }
}
