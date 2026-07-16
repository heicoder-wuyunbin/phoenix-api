package com.phoenix.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenix.api.BaseIntegrationTest;
import com.phoenix.api.dto.request.LoginDTO;
import com.phoenix.api.dto.request.RegisterDTO;
import com.phoenix.api.entity.MemberEntity;
import com.phoenix.api.entity.UserEntity;
import com.phoenix.api.mapper.MemberMapper;
import com.phoenix.api.mapper.UserMapper;
import com.phoenix.api.util.MD5Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("用户模块集成测试")
@SuppressWarnings("null")
class UserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        userMapper.delete(null);
        memberMapper.delete(null);
    }

    @Test
    @DisplayName("完整注册流程 - 手机号注册")
    void register_withMobile_success() throws Exception {
        // 预填充 Redis 验证码和短信码
        String captchaKey = "test-captcha-key";
        String captchaCode = "abcd";
        String mobile = "13800138000";
        String mobileCode = "1234";
        
        redisTemplate.opsForValue().set("captcha:" + captchaKey, captchaCode, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("code:" + mobile, mobileCode, 5, TimeUnit.MINUTES);
        
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setRegType("mobile");
        registerDTO.setUsername("testuser");
        registerDTO.setPassword("password123");
        registerDTO.setRepassword("password123");
        registerDTO.setMobile(mobile);
        registerDTO.setMobileCode(mobileCode);
        registerDTO.setCaptcha(captchaCode);
        registerDTO.setCaptchaKey(captchaKey);

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("登录流程 - 使用已存在的用户")
    void login_withExistingUser_success() throws Exception {
        // 准备测试数据
        UserEntity user = new UserEntity();
        user.setUsername("loginuser");
        user.setPassword(MD5Util.encode("password123"));
        userMapper.insert(user);

        MemberEntity member = new MemberEntity();
        member.setUserId(user.getId());
        member.setStatus(1);
        member.setMobile("13900139000");
        member.setEmail("login@example.com");
        memberMapper.insert(member);

        // 执行登录
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setLoginInfo("loginuser");
        loginDTO.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(user.getId()))
                .andExpect(jsonPath("$.data.username").value("loginuser"))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        // 验证返回的 token 不为空
        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("token"));
    }

    @Test
    @DisplayName("登录流程 - 使用邮箱登录")
    void login_withEmail_success() throws Exception {
        // 准备测试数据
        UserEntity user = new UserEntity();
        user.setUsername("emailuser");
        user.setPassword(MD5Util.encode("password123"));
        userMapper.insert(user);

        MemberEntity member = new MemberEntity();
        member.setUserId(user.getId());
        member.setStatus(1);
        member.setEmail("email@example.com");
        memberMapper.insert(member);

        // 使用邮箱登录
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setLoginInfo("email@example.com");
        loginDTO.setPassword("password123");

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(user.getId()))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    @DisplayName("登录流程 - 使用手机号登录")
    void login_withMobile_success() throws Exception {
        // 准备测试数据
        UserEntity user = new UserEntity();
        user.setUsername("mobileuser");
        user.setPassword(MD5Util.encode("password123"));
        userMapper.insert(user);

        MemberEntity member = new MemberEntity();
        member.setUserId(user.getId());
        member.setStatus(1);
        member.setMobile("13700137000");
        memberMapper.insert(member);

        // 使用手机号登录
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setLoginInfo("13700137000");
        loginDTO.setPassword("password123");

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(user.getId()))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void login_userNotFound_returnsError() throws Exception {
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
    @DisplayName("登录失败 - 密码错误")
    void login_wrongPassword_returnsError() throws Exception {
        // 准备测试数据
        UserEntity user = new UserEntity();
        user.setUsername("wrongpwduser");
        user.setPassword(MD5Util.encode("correctpassword"));
        userMapper.insert(user);

        MemberEntity member = new MemberEntity();
        member.setUserId(user.getId());
        member.setStatus(1);
        member.setMobile("13600136000");
        memberMapper.insert(member);

        // 使用错误密码登录
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setLoginInfo("wrongpwduser");
        loginDTO.setPassword("wrongpassword");

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("未找到用户信息"));
    }

    @Test
    @DisplayName("登出流程")
    void logout_success() throws Exception {
        mockMvc.perform(post("/api/user/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
