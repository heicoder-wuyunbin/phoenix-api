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

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountController 单元测试")
class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private com.phoenix.api.service.AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/account/log - 获取余额交易记录成功")
    void log_validRequest_returnsSuccess() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("total", 10);
        result.put("current", 1);
        result.put("pages", 1);
        result.put("balance", 500.00);
        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("id", 1);
        record.put("type", "recharge");
        record.put("amount", 100.00);
        record.put("message", "在线充值");
        record.put("time", "2026-07-17 10:00:00");
        records.add(record);
        result.put("records", records);

        when(accountService.getLog(eq(1L), eq(1), eq(10))).thenReturn(result);

        mockMvc.perform(get("/api/account/log")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(10))
                .andExpect(jsonPath("$.data.balance").value(500.00))
                .andExpect(jsonPath("$.data.records[0].type").value("recharge"));
    }

    @Test
    @DisplayName("GET /api/account/log - 无交易记录时返回空列表")
    void log_noRecords_returnsEmptyList() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("current", 1);
        result.put("pages", 0);
        result.put("balance", 0);
        result.put("records", Collections.emptyList());

        when(accountService.getLog(eq(1L), eq(1), eq(10))).thenReturn(result);

        mockMvc.perform(get("/api/account/log")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.balance").value(0));
    }

    @Test
    @DisplayName("POST /api/account/recharge - 提交充值申请成功")
    void recharge_validRequest_returnsSuccess() throws Exception {
        when(accountService.recharge(eq(1L), anyMap())).thenReturn("R20260717001");

        String requestBody = "{\"amount\":100.00,\"paymentId\":1}";

        mockMvc.perform(post("/api/account/recharge")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("R20260717001"));
    }

    @Test
    @DisplayName("POST /api/account/recharge - 充值金额为负数返回错误")
    void recharge_negativeAmount_returnsError() throws Exception {
        doThrow(new BusinessException(400, "充值金额必须大于0"))
                .when(accountService).recharge(eq(1L), anyMap());

        String requestBody = "{\"amount\":-50.00,\"paymentId\":1}";

        mockMvc.perform(post("/api/account/recharge")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("充值金额必须大于0"));
    }

    @Test
    @DisplayName("POST /api/account/recharge - 充值金额为零返回错误")
    void recharge_zeroAmount_returnsError() throws Exception {
        doThrow(new BusinessException(400, "充值金额必须大于0"))
                .when(accountService).recharge(eq(1L), anyMap());

        String requestBody = "{\"amount\":0.00,\"paymentId\":1}";

        mockMvc.perform(post("/api/account/recharge")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("充值金额必须大于0"));
    }

    @Test
    @DisplayName("GET /api/account/log - 未登录返回 401")
    void log_noAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/account/log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("POST /api/account/recharge - 未登录返回 401")
    void recharge_noAuth_returnsUnauthorized() throws Exception {
        String requestBody = "{\"amount\":100.00,\"paymentId\":1}";

        mockMvc.perform(post("/api/account/recharge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}