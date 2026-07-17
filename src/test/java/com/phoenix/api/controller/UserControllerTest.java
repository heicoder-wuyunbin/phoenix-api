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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController 单元测试")
@SuppressWarnings("null")
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

    // ==================== 密码修改 ====================

    @Test
    @DisplayName("PUT /api/user/password - 修改密码成功")
    void password_validRequest_returnsSuccess() throws Exception {
        doNothing().when(userService).updatePassword(eq(1L), anyMap());

        String requestBody = "{\"fpassword\":\"oldPwd123\",\"password\":\"newPwd456\",\"repassword\":\"newPwd456\"}";

        mockMvc.perform(put("/api/user/password")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("密码修改成功"));
    }

    @Test
    @DisplayName("PUT /api/user/password - 原密码错误返回错误")
    void password_wrongOldPassword_returnsError() throws Exception {
        doThrow(new BusinessException(400, "原始密码输入错误"))
                .when(userService).updatePassword(eq(1L), anyMap());

        String requestBody = "{\"fpassword\":\"wrongPwd\",\"password\":\"newPwd456\",\"repassword\":\"newPwd456\"}";

        mockMvc.perform(put("/api/user/password")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("原始密码输入错误"));
    }

    @Test
    @DisplayName("PUT /api/user/password - 新密码与确认密码不一致返回错误")
    void password_passwordMismatch_returnsError() throws Exception {
        doThrow(new BusinessException(400, "二次密码输入的不一致"))
                .when(userService).updatePassword(eq(1L), anyMap());

        String requestBody = "{\"fpassword\":\"correctPwd\",\"password\":\"newPwd456\",\"repassword\":\"differentPwd\"}";

        mockMvc.perform(put("/api/user/password")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("二次密码输入的不一致"));
    }

    @Test
    @DisplayName("PUT /api/user/password - 新密码格式不合法返回错误")
    void password_invalidFormat_returnsError() throws Exception {
        doThrow(new BusinessException(400, "密码格式不正确"))
                .when(userService).updatePassword(eq(1L), anyMap());

        String requestBody = "{\"fpassword\":\"correctPwd\",\"password\":\"123\",\"repassword\":\"123\"}";

        mockMvc.perform(put("/api/user/password")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("密码格式不正确"));
    }

    @Test
    @DisplayName("PUT /api/user/password - 未登录返回 401")
    void password_noAuth_returnsUnauthorized() throws Exception {
        String requestBody = "{\"fpassword\":\"oldPwd\",\"password\":\"newPwd\",\"repassword\":\"newPwd\"}";

        mockMvc.perform(put("/api/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    // ==================== 头像上传 ====================

    @Test
    @DisplayName("POST /api/user/avatar - 上传头像成功")
    void avatar_validRequest_returnsSuccess() throws Exception {
        when(userService.updateAvatar(eq(1L), any())).thenReturn("/api/upload/proxy/avatar.jpg");

        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", "test-image-content".getBytes());

        mockMvc.perform(multipart("/api/user/avatar")
                        .file(file)
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("/api/upload/proxy/avatar.jpg"));
    }

    @Test
    @DisplayName("POST /api/user/avatar - 未登录返回 401")
    void avatar_noAuth_returnsUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", "test-image-content".getBytes());

        mockMvc.perform(multipart("/api/user/avatar")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("POST /api/user/avatar - 上传不支持的格式返回错误")
    void avatar_unsupportedFormat_returnsError() throws Exception {
        doThrow(new BusinessException(400, "不支持的图片格式"))
                .when(userService).updateAvatar(eq(1L), any());

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test-content".getBytes());

        mockMvc.perform(multipart("/api/user/avatar")
                        .file(file)
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不支持的图片格式"));
    }

    @Test
    @DisplayName("POST /api/user/avatar - 上传文件过大返回错误")
    void avatar_fileTooLarge_returnsError() throws Exception {
        doThrow(new BusinessException(400, "文件大小超过限制"))
                .when(userService).updateAvatar(eq(1L), any());

        byte[] largeContent = new byte[11 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.jpg", "image/jpeg", largeContent);

        mockMvc.perform(multipart("/api/user/avatar")
                        .file(file)
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("文件大小超过限制"));
    }
}
