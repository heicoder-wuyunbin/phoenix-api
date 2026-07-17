package com.phoenix.api.controller;

import com.phoenix.api.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PropController 单元测试")
class PropControllerTest {

    private MockMvc mockMvc;

    @Mock
    private com.phoenix.api.service.PropService propService;

    @InjectMocks
    private PropController propController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(propController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/prop/list - 获取代金券列表成功")
    void list_validRequest_returnsSuccess() throws Exception {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("total", 5);
        pageResult.put("current", 1);
        pageResult.put("pages", 1);
        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> prop = new HashMap<>();
        prop.put("id", 1);
        prop.put("name", "10元优惠券");
        prop.put("value", 10.00);
        prop.put("condition", "满100可用");
        prop.put("status", "available");
        prop.put("startTime", "2026-07-01");
        prop.put("endTime", "2026-08-01");
        records.add(prop);
        pageResult.put("records", records);

        when(propService.getList(eq(1L), isNull(), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/api/prop/list")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.records[0].name").value("10元优惠券"));
    }

    @Test
    @DisplayName("GET /api/prop/list - 按状态筛选成功")
    void list_filterByStatus_returnsFiltered() throws Exception {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("total", 3);
        pageResult.put("current", 1);
        pageResult.put("pages", 1);
        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> prop = new HashMap<>();
        prop.put("id", 1);
        prop.put("name", "5元优惠券");
        prop.put("status", "available");
        records.add(prop);
        pageResult.put("records", records);

        when(propService.getList(eq(1L), eq("available"), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/api/prop/list")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .param("status", "available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(3));
    }

    @Test
    @DisplayName("GET /api/prop/list - 无代金券时返回空列表")
    void list_noProps_returnsEmptyList() throws Exception {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("total", 0);
        pageResult.put("current", 1);
        pageResult.put("pages", 0);
        pageResult.put("records", Collections.emptyList());

        when(propService.getList(eq(1L), isNull(), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/api/prop/list")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("GET /api/prop/list - 未登录返回 401")
    void list_noAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/prop/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}