package com.phoenix.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phoenix.api.exception.GlobalExceptionHandler;
import com.phoenix.api.service.GoodsService;
import com.phoenix.api.vo.GoodsVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoodsController 单元测试")
class GoodsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GoodsService goodsService;

    @InjectMocks
    private GoodsController goodsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(goodsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/goods/list - 默认参数返回商品列表")
    void list_defaultParams_returnsGoodsList() throws Exception {
        GoodsVO goods1 = new GoodsVO();
        goods1.setId(1L);
        goods1.setName("商品A");
        goods1.setPrice(new BigDecimal("99.99"));
        goods1.setSales(100);
        goods1.setStock(50);

        GoodsVO goods2 = new GoodsVO();
        goods2.setId(2L);
        goods2.setName("商品B");
        goods2.setPrice(new BigDecimal("199.99"));
        goods2.setSales(200);
        goods2.setStock(30);

        Page<GoodsVO> page = new Page<>(1, 10, 2);
        page.setRecords(Arrays.asList(goods1, goods2));

        when(goodsService.getGoodsList(eq(0L), eq(1), eq(10))).thenReturn(page);

        mockMvc.perform(get("/api/goods/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.records[0].name").value("商品A"))
                .andExpect(jsonPath("$.data.records[1].name").value("商品B"));
    }

    @Test
    @DisplayName("GET /api/goods/list - 指定分类ID返回商品列表")
    void list_withCategoryId_returnsGoodsList() throws Exception {
        GoodsVO goods = new GoodsVO();
        goods.setId(1L);
        goods.setName("分类商品");
        goods.setPrice(new BigDecimal("59.99"));

        Page<GoodsVO> page = new Page<>(1, 10, 1);
        page.setRecords(Collections.singletonList(goods));

        when(goodsService.getGoodsList(eq(5L), eq(1), eq(10))).thenReturn(page);

        mockMvc.perform(get("/api/goods/list")
                        .param("categoryId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].name").value("分类商品"));
    }

    @Test
    @DisplayName("GET /api/goods/list - 自定义分页参数")
    void list_customPagination_returnsGoodsList() throws Exception {
        Page<GoodsVO> page = new Page<>(2, 5, 12);
        page.setRecords(Collections.emptyList());

        when(goodsService.getGoodsList(eq(0L), eq(2), eq(5))).thenReturn(page);

        mockMvc.perform(get("/api/goods/list")
                        .param("pageNum", "2")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(0));
    }
}
