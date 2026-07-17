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
@DisplayName("WithdrawController 单元测试")
class WithdrawControllerTest {

    private MockMvc mockMvc;

    @Mock
    private com.phoenix.api.service.WithdrawService withdrawService;

    @InjectMocks
    private WithdrawController withdrawController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(withdrawController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/withdraw - 提交提现申请成功")
    void add_validRequest_returnsSuccess() throws Exception {
        doNothing().when(withdrawService).add(eq(1L), anyMap());

        String requestBody = "{\"amount\":50.00,\"account\":\"6222021234567890\",\"accountName\":\"张三\",\"bankName\":\"中国工商银行\"}";

        mockMvc.perform(post("/api/withdraw")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("提现申请提交成功"));
    }

    @Test
    @DisplayName("POST /api/withdraw - 提现金额超过余额返回错误")
    void add_insufficientBalance_returnsError() throws Exception {
        doThrow(new BusinessException(400, "提现金额不能超过可用余额"))
                .when(withdrawService).add(eq(1L), anyMap());

        String requestBody = "{\"amount\":200.00,\"account\":\"6222021234567890\"}";

        mockMvc.perform(post("/api/withdraw")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("提现金额不能超过可用余额"));
    }

    @Test
    @DisplayName("POST /api/withdraw - 提现金额为负数返回错误")
    void add_negativeAmount_returnsError() throws Exception {
        doThrow(new BusinessException(400, "提现金额必须大于0"))
                .when(withdrawService).add(eq(1L), anyMap());

        String requestBody = "{\"amount\":-10.00}";

        mockMvc.perform(post("/api/withdraw")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("提现金额必须大于0"));
    }

    @Test
    @DisplayName("GET /api/withdraw/list - 获取提现记录列表成功")
    void list_validRequest_returnsSuccess() throws Exception {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("total", 5);
        pageResult.put("current", 1);
        pageResult.put("pages", 1);
        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("id", 1);
        record.put("amount", 50.00);
        record.put("status", "pending");
        record.put("account", "622202****7890");
        record.put("createTime", "2026-07-17 10:00:00");
        records.add(record);
        pageResult.put("records", records);

        when(withdrawService.getList(eq(1L), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/api/withdraw/list")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(5));
    }

    @Test
    @DisplayName("DELETE /api/withdraw/{id} - 取消提现申请成功")
    void delete_validRequest_returnsSuccess() throws Exception {
        doNothing().when(withdrawService).delete(1L, 1L);

        mockMvc.perform(delete("/api/withdraw/1")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("取消成功"));
    }

    @Test
    @DisplayName("DELETE /api/withdraw/{id} - 取消已审核通过的提现返回错误")
    void delete_approvedWithdraw_returnsError() throws Exception {
        doThrow(new BusinessException(400, "当前提现状态不允许取消"))
                .when(withdrawService).delete(1L, 2L);

        mockMvc.perform(delete("/api/withdraw/2")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("当前提现状态不允许取消"));
    }

    @Test
    @DisplayName("POST /api/withdraw - 未登录返回 401")
    void add_noAuth_returnsUnauthorized() throws Exception {
        String requestBody = "{\"amount\":50.00}";

        mockMvc.perform(post("/api/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}