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
@DisplayName("EvaluationController 单元测试")
class EvaluationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private com.phoenix.api.service.EvaluationService evaluationService;

    @InjectMocks
    private EvaluationController evaluationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(evaluationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/evaluation/list - 获取待评价列表成功")
    void list_pending_returnsSuccess() throws Exception {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("total", 3);
        pageResult.put("current", 1);
        pageResult.put("pages", 1);
        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("orderGoodsId", 1);
        item.put("goodsName", "测试商品");
        item.put("goodsImg", "http://example.com/img.jpg");
        item.put("goodsPrice", 99.99);
        records.add(item);
        pageResult.put("records", records);

        when(evaluationService.getList(eq(1L), eq("pending"), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/api/evaluation/list")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .param("type", "pending")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.records[0].goodsName").value("测试商品"));
    }

    @Test
    @DisplayName("GET /api/evaluation/list - 获取已评价列表成功")
    void list_done_returnsSuccess() throws Exception {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("total", 2);
        pageResult.put("current", 1);
        pageResult.put("pages", 1);
        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("orderGoodsId", 1);
        item.put("content", "商品质量很好");
        item.put("point", 5);
        item.put("createTime", "2026-07-17 10:00:00");
        records.add(item);
        pageResult.put("records", records);

        when(evaluationService.getList(eq(1L), eq("done"), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/api/evaluation/list")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .param("type", "done"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].point").value(5));
    }

    @Test
    @DisplayName("POST /api/evaluation - 发表评价成功")
    void add_validRequest_returnsSuccess() throws Exception {
        String requestBody = "{\"orderGoodsId\":1,\"content\":\"商品质量很好，物流很快！\",\"point\":5}";

        doNothing().when(evaluationService).add(eq(1L), anyMap());

        mockMvc.perform(post("/api/evaluation")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("评价成功"));
    }

    @Test
    @DisplayName("POST /api/evaluation - 重复评价已评价商品返回错误")
    void add_duplicateEvaluation_returnsError() throws Exception {
        String requestBody = "{\"orderGoodsId\":1,\"content\":\"再次评价\",\"point\":4}";

        doThrow(new BusinessException(400, "该商品已评价"))
                .when(evaluationService).add(eq(1L), anyMap());

        mockMvc.perform(post("/api/evaluation")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("该商品已评价"));
    }

    @Test
    @DisplayName("POST /api/evaluation - 评分超出范围返回错误")
    void add_invalidPoint_returnsError() throws Exception {
        String requestBody = "{\"orderGoodsId\":2,\"content\":\"较好\",\"point\":6}";

        doThrow(new BusinessException(400, "评分必须在1-5之间"))
                .when(evaluationService).add(eq(1L), anyMap());

        mockMvc.perform(post("/api/evaluation")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("评分必须在1-5之间"));
    }

    @Test
    @DisplayName("GET /api/evaluation/list - 未登录返回 401")
    void list_noAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/evaluation/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("POST /api/evaluation - 未登录返回 401")
    void add_noAuth_returnsUnauthorized() throws Exception {
        String requestBody = "{\"orderGoodsId\":1,\"content\":\"好\",\"point\":5}";

        mockMvc.perform(post("/api/evaluation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}