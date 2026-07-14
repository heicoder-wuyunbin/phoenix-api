package com.phoenix.api.controller;

import com.phoenix.api.entity.CategoryEntity;
import com.phoenix.api.exception.GlobalExceptionHandler;
import com.phoenix.api.service.GoodsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryController 单元测试")
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GoodsService goodsService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/category/list - 返回分类列表")
    void list_returnsCategoryList() throws Exception {
        CategoryEntity cat1 = new CategoryEntity();
        cat1.setId(1L);
        cat1.setName("电子产品");
        cat1.setParentId(0);
        cat1.setSort(1);

        CategoryEntity cat2 = new CategoryEntity();
        cat2.setId(2L);
        cat2.setName("服装");
        cat2.setParentId(0);
        cat2.setSort(2);

        List<CategoryEntity> categories = Arrays.asList(cat1, cat2);
        when(goodsService.getCategoryList()).thenReturn(categories);

        mockMvc.perform(get("/api/category/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("电子产品"))
                .andExpect(jsonPath("$.data[1].name").value("服装"));
    }

    @Test
    @DisplayName("GET /api/category/list - 无分类时返回空列表")
    void list_noCategories_returnsEmptyList() throws Exception {
        when(goodsService.getCategoryList()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/category/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
