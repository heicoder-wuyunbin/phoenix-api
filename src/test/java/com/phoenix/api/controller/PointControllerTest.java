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
@DisplayName("PointController 单元测试")
class PointControllerTest {

    private MockMvc mockMvc;

    @Mock
    private com.phoenix.api.service.PointService pointService;

    @InjectMocks
    private PointController pointController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pointController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/point/log - 获取积分记录成功")
    void log_validRequest_returnsSuccess() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("total", 8);
        result.put("current", 1);
        result.put("pages", 1);
        result.put("currentPoint", 2000);
        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("id", 1);
        record.put("type", "earn");
        record.put("point", 100);
        record.put("message", "购物获得积分");
        record.put("time", "2026-07-17 10:00:00");
        records.add(record);
        result.put("records", records);

        when(pointService.getLog(eq(1L), eq(1), eq(10))).thenReturn(result);

        mockMvc.perform(get("/api/point/log")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(8))
                .andExpect(jsonPath("$.data.currentPoint").value(2000))
                .andExpect(jsonPath("$.data.records[0].type").value("earn"));
    }

    @Test
    @DisplayName("GET /api/point/log - 无积分记录时返回空列表")
    void log_noRecords_returnsEmptyList() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("current", 1);
        result.put("pages", 0);
        result.put("currentPoint", 0);
        result.put("records", Collections.emptyList());

        when(pointService.getLog(eq(1L), eq(1), eq(10))).thenReturn(result);

        mockMvc.perform(get("/api/point/log")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.currentPoint").value(0));
    }

    @Test
    @DisplayName("POST /api/point/exchange - 积分兑换代金券成功")
    void exchange_validRequest_returnsSuccess() throws Exception {
        doNothing().when(pointService).exchange(eq(1L), eq(1));

        String requestBody = "{\"propId\":1}";

        mockMvc.perform(post("/api/point/exchange")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("兑换成功"));
    }

    @Test
    @DisplayName("POST /api/point/exchange - 积分不足返回错误")
    void exchange_insufficientPoints_returnsError() throws Exception {
        doThrow(new BusinessException(400, "积分不足"))
                .when(pointService).exchange(eq(1L), eq(1));

        String requestBody = "{\"propId\":1}";

        mockMvc.perform(post("/api/point/exchange")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("积分不足"));
    }

    @Test
    @DisplayName("POST /api/point/exchange - 代金券库存不足返回错误")
    void exchange_outOfStock_returnsError() throws Exception {
        doThrow(new BusinessException(400, "代金券已兑完"))
                .when(pointService).exchange(eq(1L), eq(2));

        String requestBody = "{\"propId\":2}";

        mockMvc.perform(post("/api/point/exchange")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("代金券已兑完"));
    }

    @Test
    @DisplayName("GET /api/point/log - 未登录返回 401")
    void log_noAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/point/log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("POST /api/point/exchange - 未登录返回 401")
    void exchange_noAuth_returnsUnauthorized() throws Exception {
        String requestBody = "{\"propId\":1}";

        mockMvc.perform(post("/api/point/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}