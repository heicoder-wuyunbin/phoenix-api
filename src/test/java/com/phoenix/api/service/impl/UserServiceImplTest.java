package com.phoenix.api.service.impl;

import com.phoenix.api.config.SiteConfig;
import com.phoenix.api.dto.request.LoginDTO;
import com.phoenix.api.dto.request.RegisterDTO;
import com.phoenix.api.dto.response.LoginResponseDTO;
import com.phoenix.api.entity.MemberEntity;
import com.phoenix.api.entity.UserEntity;
import com.phoenix.api.exception.BusinessException;
import com.phoenix.api.mapper.MemberMapper;
import com.phoenix.api.mapper.UserGroupMapper;
import com.phoenix.api.mapper.UserMapper;
import com.phoenix.api.util.CaptchaUtil;
import com.phoenix.api.util.JwtUtil;
import com.phoenix.api.util.MD5Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl 单元测试")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private UserGroupMapper userGroupMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SiteConfig siteConfig;

    @Mock
    private CaptchaUtil captchaUtil;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("login - 登录成功返回用户信息和 Token")
    void login_validCredentials_returnsLoginResponse() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(MD5Util.encode("password123"));

        when(userMapper.findByLoginInfo("testuser")).thenReturn(user);
        when(jwtUtil.generateToken(1L, "testuser")).thenReturn("jwt-token");

        MemberEntity member = new MemberEntity();
        member.setUserId(1L);
        member.setExp(0);
        when(memberMapper.selectOne(any())).thenReturn(member);
        when(userGroupMapper.findGroupIdByExp(0)).thenReturn(1L);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setLoginInfo("testuser");
        loginDTO.setPassword("password123");

        LoginResponseDTO response = userService.login(loginDTO);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    @DisplayName("login - 用户不存在应抛出异常")
    void login_userNotFound_throwsException() {
        when(userMapper.findByLoginInfo("nonexistent")).thenReturn(null);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setLoginInfo("nonexistent");
        loginDTO.setPassword("password123");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.login(loginDTO));
        assertEquals("未找到用户信息", exception.getMessage());
    }

    @Test
    @DisplayName("login - 密码错误应抛出异常")
    void login_wrongPassword_throwsException() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(MD5Util.encode("correctpassword"));

        when(userMapper.findByLoginInfo("testuser")).thenReturn(user);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setLoginInfo("testuser");
        loginDTO.setPassword("wrongpassword");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.login(loginDTO));
        assertEquals("未找到用户信息", exception.getMessage());
    }

    @Test
    @DisplayName("register - 网站禁止注册应抛出异常")
    void register_registrationDisabled_throwsException() {
        when(siteConfig.getRegOption()).thenReturn(2);

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.register(registerDTO));
        assertEquals("当前网站禁止新用户注册", exception.getMessage());
    }

    @Test
    @DisplayName("register - 密码不一致应抛出异常")
    void register_passwordMismatch_throwsException() {
        when(siteConfig.getRegOption()).thenReturn(3);

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("password123");
        registerDTO.setRepassword("different");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.register(registerDTO));
        assertEquals("2次密码输入不一致", exception.getMessage());
    }

    @Test
    @DisplayName("register - 验证码错误应抛出异常")
    void register_wrongCaptcha_throwsException() {
        when(siteConfig.getRegOption()).thenReturn(3);
        when(captchaUtil.verify("captcha-key", "wrong")).thenReturn(false);

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("password123");
        registerDTO.setRepassword("password123");
        registerDTO.setCaptcha("wrong");
        registerDTO.setCaptchaKey("captcha-key");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.register(registerDTO));
        assertEquals("图形验证码输入不正确", exception.getMessage());
    }

    @Test
    @DisplayName("sendMobileCode - 手机号格式错误应抛出异常")
    void sendMobileCode_invalidMobile_throwsException() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.sendMobileCode("12345", "captcha-key", "abcd"));
        assertEquals("请填写正确的手机号码", exception.getMessage());
    }

    @Test
    @DisplayName("sendMobileCode - 验证码错误应抛出异常")
    void sendMobileCode_wrongCaptcha_throwsException() {
        when(captchaUtil.verify("captcha-key", "wrong")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.sendMobileCode("13800138000", "captcha-key", "wrong"));
        assertEquals("请填写正确的图形验证码", exception.getMessage());
    }

    @Test
    @DisplayName("sendMobileCode - 手机号已注册应抛出异常")
    void sendMobileCode_mobileAlreadyRegistered_throwsException() {
        when(captchaUtil.verify("captcha-key", "abcd")).thenReturn(true);

        MemberEntity existingMember = new MemberEntity();
        when(memberMapper.findByMobile("13800138000")).thenReturn(existingMember);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.sendMobileCode("13800138000", "captcha-key", "abcd"));
        assertEquals("手机号已经被注册", exception.getMessage());
    }

    @Test
    @DisplayName("checkMail - 无效验证码应抛出异常")
    void checkMail_invalidCode_throwsException() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.checkMail("invalid-code"));
        assertEquals("验证信息有误，请核实！", exception.getMessage());
    }

    @Test
    @DisplayName("sendCheckMail - 邮箱格式错误应抛出异常")
    void sendCheckMail_invalidEmail_throwsException() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.sendCheckMail("invalid-email"));
        assertEquals("邮件格式错误", exception.getMessage());
    }

    @Test
    @DisplayName("sendCheckMail - 用户不存在应抛出异常")
    void sendCheckMail_userNotFound_throwsException() {
        when(memberMapper.findByEmail("test@example.com")).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.sendCheckMail("test@example.com"));
        assertEquals("用户信息不存在", exception.getMessage());
    }
}
