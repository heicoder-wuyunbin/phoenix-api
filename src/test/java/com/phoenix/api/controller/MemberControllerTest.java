package com.phoenix.api.controller;

import com.phoenix.api.exception.BusinessException;
import com.phoenix.api.exception.GlobalExceptionHandler;
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

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberController 单元测试")
class MemberControllerTest {

    private MockMvc mockMvc;

    @Mock
    private com.phoenix.api.service.MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/member/center - 获取用户中心首页数据成功")
    void center_validRequest_returnsSuccess() throws Exception {
        Map<String, Object> centerData = new HashMap<>();
        centerData.put("username", "testuser");
        centerData.put("balance", 100.50);
        centerData.put("point", 500);
        centerData.put("propNum", 2);
        centerData.put("headIco", "http://example.com/avatar.jpg");
        Map<String, Integer> orderCount = new HashMap<>();
        orderCount.put("waitPay", 1);
        orderCount.put("waitDeliver", 2);
        orderCount.put("waitReceive", 0);
        orderCount.put("waitEvaluate", 1);
        centerData.put("orderCount", orderCount);
        centerData.put("msgNum", 3);

        when(memberService.getCenter(1L)).thenReturn(centerData);

        mockMvc.perform(get("/api/member/center")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.balance").value(100.50))
                .andExpect(jsonPath("$.data.point").value(500))
                .andExpect(jsonPath("$.data.propNum").value(2))
                .andExpect(jsonPath("$.data.msgNum").value(3));
    }

    @Test
    @DisplayName("GET /api/member/center - 未登录返回 401")
    void center_noAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/member/center"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("GET /api/member/profile - 获取个人资料成功")
    void profile_validRequest_returnsSuccess() throws Exception {
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", "testuser");
        profile.put("trueName", "张三");
        profile.put("sex", 1);
        profile.put("mobile", "13800138000");
        profile.put("email", "test@example.com");

        when(memberService.getProfile(1L)).thenReturn(profile);

        mockMvc.perform(get("/api/member/profile")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.trueName").value("张三"))
                .andExpect(jsonPath("$.data.sex").value(1));
    }

    @Test
    @DisplayName("GET /api/member/profile - 未登录返回 401")
    void profile_noAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/member/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("PUT /api/member/profile - 修改个人资料成功")
    void updateProfile_validRequest_returnsSuccess() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("trueName", "李四");
        updates.put("sex", 2);
        updates.put("mobile", "13900139000");

        String requestBody = "{\"trueName\":\"李四\",\"sex\":2,\"mobile\":\"13900139000\"}";

        doNothing().when(memberService).updateProfile(eq(1L), anyMap());

        mockMvc.perform(put("/api/member/profile")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    @DisplayName("PUT /api/member/profile - 邮箱已被注册返回错误")
    void updateProfile_emailExists_returnsError() throws Exception {
        String requestBody = "{\"email\":\"exists@example.com\"}";

        doThrow(new BusinessException("邮箱已经被注册"))
                .when(memberService).updateProfile(eq(1L), anyMap());

        mockMvc.perform(put("/api/member/profile")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("邮箱已经被注册"));
    }

    @Test
    @DisplayName("PUT /api/member/profile - 未登录返回 401")
    void updateProfile_noAuth_returnsUnauthorized() throws Exception {
        String requestBody = "{\"trueName\":\"李四\"}";

        mockMvc.perform(put("/api/member/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}