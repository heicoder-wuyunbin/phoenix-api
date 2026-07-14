package com.phoenix.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenix.api.dto.request.LoginDTO;
import com.phoenix.api.dto.request.RegisterDTO;
import com.phoenix.api.dto.response.LoginResponseDTO;
import com.phoenix.api.exception.BusinessException;
import com.phoenix.api.exception.GlobalExceptionHandler;
import com.phoenix.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController 单元测试")
class UserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/user/login - 登录成功")
    void login_validRequest_returnsSuccess() throws Exception {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setUserId(1L);
        response.setUsername("testuser");
        response.setToken("jwt-token");

        when(userService.login(any(LoginDTO.class))).thenReturn(response);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setLoginInfo("testuser");
        loginDTO.setPassword("password123");

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    @DisplayName("POST /api/user/login - 用户不存在应返回错误")
    void login_userNotFound_returnsError() throws Exception {
        when(userService.login(any(LoginDTO.class)))
                .thenThrow(new BusinessException("未找到用户信息"));

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setLoginInfo("nonexistent");
        loginDTO.setPassword("password123");

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("未找到用户信息"));
    }

    @Test
    @DisplayName("POST /api/user/login - 缺少参数应返回 400")
    void login_missingFields_returnsBadRequest() throws Exception {
        LoginDTO loginDTO = new LoginDTO();

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("POST /api/user/register - 注册成功")
    void register_validRequest_returnsSuccess() throws Exception {
        doNothing().when(userService).register(any(RegisterDTO.class));

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("password123");
        registerDTO.setRepassword("password123");
        registerDTO.setEmail("test@example.com");
        registerDTO.setCaptcha("abcd");
        registerDTO.setCaptchaKey("captcha-key");

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("POST /api/user/register - 密码不一致应返回错误")
    void register_passwordMismatch_returnsError() throws Exception {
        doThrow(new BusinessException("2次密码输入不一致"))
                .when(userService).register(any(RegisterDTO.class));

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("password123");
        registerDTO.setRepassword("different");
        registerDTO.setEmail("test@example.com");
        registerDTO.setCaptcha("abcd");
        registerDTO.setCaptchaKey("captcha-key");

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("2次密码输入不一致"));
    }

    @Test
    @DisplayName("POST /api/user/send-mobile-code - 发送验证码成功")
    void sendMobileCode_validRequest_returnsSuccess() throws Exception {
        doNothing().when(userService).sendMobileCode("13800138000", "captcha-key", "abcd");

        mockMvc.perform(post("/api/user/send-mobile-code")
                        .param("mobile", "13800138000")
                        .param("captchaKey", "captcha-key")
                        .param("captcha", "abcd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("POST /api/user/logout - 登出成功")
    void logout_returnsSuccess() throws Exception {
        doNothing().when(userService).logout();

        mockMvc.perform(post("/api/user/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("GET /api/user/check-mail - 邮箱验证成功")
    void checkMail_validCode_returnsSuccess() throws Exception {
        doNothing().when(userService).checkMail("test-code");

        mockMvc.perform(get("/api/user/check-mail")
                        .param("code", "test-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("POST /api/user/send-check-mail - 发送验证邮件成功")
    void sendCheckMail_validEmail_returnsSuccess() throws Exception {
        doNothing().when(userService).sendCheckMail("test@example.com");

        mockMvc.perform(post("/api/user/send-check-mail")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("POST /api/user/oauth-login - TODO 接口返回成功")
    void oauthLogin_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/user/oauth-login")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("GET /api/user/oauth-callback - TODO 接口返回成功")
    void oauthCallback_returnsSuccess() throws Exception {
        mockMvc.perform(get("/api/user/oauth-callback")
                        .param("oauthName", "github"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
