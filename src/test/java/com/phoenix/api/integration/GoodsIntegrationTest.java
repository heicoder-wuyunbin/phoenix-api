package com.phoenix.api.integration;

import com.phoenix.api.BaseIntegrationTest;
import com.phoenix.api.entity.CategoryEntity;
import com.phoenix.api.entity.CategoryExtendEntity;
import com.phoenix.api.entity.GoodsEntity;
import com.phoenix.api.mapper.CategoryExtendMapper;
import com.phoenix.api.mapper.CategoryMapper;
import com.phoenix.api.mapper.GoodsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("商品模块集成测试")
class GoodsIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryExtendMapper categoryExtendMapper;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        categoryExtendMapper.delete(null);
        goodsMapper.delete(null);
        categoryMapper.delete(null);
    }

    @Test
    @DisplayName("获取分类列表 - 返回顶级分类")
    void getCategoryList_returnsTopLevelCategories() throws Exception {
        // 准备测试数据
        CategoryEntity parentCategory = new CategoryEntity();
        parentCategory.setName("电子产品");
        parentCategory.setParentId(0);
        parentCategory.setSort(1);
        categoryMapper.insert(parentCategory);

        CategoryEntity childCategory = new CategoryEntity();
        childCategory.setName("手机");
        childCategory.setParentId(parentCategory.getId().intValue());
        childCategory.setSort(1);
        categoryMapper.insert(childCategory);

        // 执行请求
        mockMvc.perform(get("/api/category/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("电子产品"));
    }

    @Test
    @DisplayName("获取商品列表 - 默认分页")
    void getGoodsList_defaultPagination() throws Exception {
        // 准备测试数据
        for (int i = 1; i <= 15; i++) {
            GoodsEntity goods = new GoodsEntity();
            goods.setName("商品" + i);
            goods.setImg("img" + i + ".jpg");
            goods.setSellPrice(new BigDecimal("99.9" + i));
            goods.setStoreNums(100);
            goods.setSale(i * 10);
            goodsMapper.insert(goods);
        }

        // 执行请求 - 默认第一页，每页10条
        mockMvc.perform(get("/api/goods/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(10))
                .andExpect(jsonPath("$.data.total").value(15));
    }

    @Test
    @DisplayName("获取商品列表 - 自定义分页")
    void getGoodsList_customPagination() throws Exception {
        // 准备测试数据
        for (int i = 1; i <= 25; i++) {
            GoodsEntity goods = new GoodsEntity();
            goods.setName("商品" + i);
            goods.setImg("img" + i + ".jpg");
            goods.setSellPrice(new BigDecimal("99.9" + i));
            goods.setStoreNums(100);
            goods.setSale(i * 10);
            goodsMapper.insert(goods);
        }

        // 执行请求 - 第二页，每页5条
        mockMvc.perform(get("/api/goods/list")
                        .param("pageNum", "2")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(5))
                .andExpect(jsonPath("$.data.total").value(25));
    }

    @Test
    @DisplayName("获取商品列表 - 按分类筛选")
    void getGoodsList_filterByCategory() throws Exception {
        // 准备分类数据
        CategoryEntity category1 = new CategoryEntity();
        category1.setName("分类1");
        category1.setParentId(0);
        category1.setSort(1);
        categoryMapper.insert(category1);

        CategoryEntity category2 = new CategoryEntity();
        category2.setName("分类2");
        category2.setParentId(0);
        category2.setSort(2);
        categoryMapper.insert(category2);

        // 准备商品数据
        GoodsEntity goods1 = new GoodsEntity();
        goods1.setName("分类1商品");
        goods1.setImg("img1.jpg");
        goods1.setSellPrice(new BigDecimal("99.99"));
        goods1.setStoreNums(100);
        goods1.setSale(50);
        goodsMapper.insert(goods1);

        GoodsEntity goods2 = new GoodsEntity();
        goods2.setName("分类2商品");
        goods2.setImg("img2.jpg");
        goods2.setSellPrice(new BigDecimal("199.99"));
        goods2.setStoreNums(50);
        goods2.setSale(100);
        goodsMapper.insert(goods2);

        // 关联分类
        CategoryExtendEntity extend1 = new CategoryExtendEntity();
        extend1.setGoodsId(goods1.getId());
        extend1.setCategoryId(category1.getId());
        categoryExtendMapper.insert(extend1);

        CategoryExtendEntity extend2 = new CategoryExtendEntity();
        extend2.setGoodsId(goods2.getId());
        extend2.setCategoryId(category2.getId());
        categoryExtendMapper.insert(extend2);

        // 执行请求 - 查询分类1的商品
        mockMvc.perform(get("/api/goods/list")
                        .param("categoryId", category1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("分类1商品"));
    }

    @Test
    @DisplayName("获取商品列表 - 分类下无商品")
    void getGoodsList_emptyCategory() throws Exception {
        // 准备分类数据
        CategoryEntity category = new CategoryEntity();
        category.setName("空分类");
        category.setParentId(0);
        category.setSort(1);
        categoryMapper.insert(category);

        // 执行请求
        mockMvc.perform(get("/api/goods/list")
                        .param("categoryId", category.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(0));
    }

    @Test
    @DisplayName("获取商品列表 - 按销量排序")
    void getGoodsList_orderBySales() throws Exception {
        // 准备测试数据
        GoodsEntity goods1 = new GoodsEntity();
        goods1.setName("低销量商品");
        goods1.setImg("img1.jpg");
        goods1.setSellPrice(new BigDecimal("99.99"));
        goods1.setStoreNums(100);
        goods1.setSale(10);
        goodsMapper.insert(goods1);

        GoodsEntity goods2 = new GoodsEntity();
        goods2.setName("高销量商品");
        goods2.setImg("img2.jpg");
        goods2.setSellPrice(new BigDecimal("199.99"));
        goods2.setStoreNums(50);
        goods2.setSale(1000);
        goodsMapper.insert(goods2);

        GoodsEntity goods3 = new GoodsEntity();
        goods3.setName("中销量商品");
        goods3.setImg("img3.jpg");
        goods3.setSellPrice(new BigDecimal("149.99"));
        goods3.setStoreNums(75);
        goods3.setSale(500);
        goodsMapper.insert(goods3);

        // 执行请求
        mockMvc.perform(get("/api/goods/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(3))
                .andExpect(jsonPath("$.data.records[0].name").value("高销量商品"))
                .andExpect(jsonPath("$.data.records[1].name").value("中销量商品"))
                .andExpect(jsonPath("$.data.records[2].name").value("低销量商品"));
    }
}
