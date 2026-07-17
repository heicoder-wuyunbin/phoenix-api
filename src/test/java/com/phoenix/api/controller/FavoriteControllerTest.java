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
@DisplayName("FavoriteController 单元测试")
class FavoriteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private com.phoenix.api.service.FavoriteService favoriteService;

    @InjectMocks
    private FavoriteController favoriteController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(favoriteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/favorite/list - 获取收藏列表成功")
    void list_validRequest_returnsSuccess() throws Exception {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("total", 5);
        pageResult.put("current", 1);
        pageResult.put("pages", 1);
        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> fav1 = new HashMap<>();
        fav1.put("id", 1);
        fav1.put("goodsName", "测试商品1");
        fav1.put("goodsPrice", 99.99);
        fav1.put("goodsImg", "http://example.com/img1.jpg");
        records.add(fav1);
        pageResult.put("records", records);

        when(favoriteService.getList(eq(1L), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/api/favorite/list")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.records[0].goodsName").value("测试商品1"));
    }

    @Test
    @DisplayName("GET /api/favorite/list - 无收藏时返回空列表")
    void list_noFavorites_returnsEmptyList() throws Exception {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("total", 0);
        pageResult.put("current", 1);
        pageResult.put("pages", 0);
        pageResult.put("records", Collections.emptyList());

        when(favoriteService.getList(eq(1L), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/api/favorite/list")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    @DisplayName("GET /api/favorite/list - 未登录返回 401")
    void list_noAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/favorite/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("DELETE /api/favorite/{id} - 取消收藏成功")
    void delete_validRequest_returnsSuccess() throws Exception {
        doNothing().when(favoriteService).delete(1L, 1L);

        mockMvc.perform(delete("/api/favorite/1")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("DELETE /api/favorite/{id} - 取消不存在的收藏返回 404")
    void delete_notFound_returnsNotFound() throws Exception {
        doThrow(new BusinessException("收藏不存在")).when(favoriteService).delete(1L, 99999L);

        mockMvc.perform(delete("/api/favorite/99999")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("PUT /api/favorite/{id}/summary - 编辑收藏备注成功")
    void updateSummary_validRequest_returnsSuccess() throws Exception {
        String requestBody = "{\"summary\":\"想买的商品，降价提醒\"}";

        doNothing().when(favoriteService).updateSummary(eq(1L), eq(2L), eq("想买的商品，降价提醒"));

        mockMvc.perform(put("/api/favorite/2/summary")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}