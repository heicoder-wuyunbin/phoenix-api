package com.phoenix.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phoenix.api.entity.CategoryEntity;
import com.phoenix.api.entity.CategoryExtendEntity;
import com.phoenix.api.entity.GoodsEntity;
import com.phoenix.api.mapper.CategoryExtendMapper;
import com.phoenix.api.mapper.CategoryMapper;
import com.phoenix.api.mapper.GoodsMapper;
import com.phoenix.api.mapper.GoodsPhotoMapper;
import com.phoenix.api.mapper.GoodsPhotoRelationMapper;
import com.phoenix.api.mapper.ProductMapper;
import com.phoenix.api.vo.GoodsVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoodsServiceImpl 单元测试")
@SuppressWarnings({"null", "unchecked"})
class GoodsServiceImplTest {

    @Mock
    private GoodsMapper goodsMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CategoryExtendMapper categoryExtendMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private GoodsPhotoMapper goodsPhotoMapper;

    @Mock
    private GoodsPhotoRelationMapper goodsPhotoRelationMapper;

    @InjectMocks
    private GoodsServiceImpl goodsService;

    @Test
    @DisplayName("getGoodsList - 不指定分类返回所有商品")
    void getGoodsList_noCategoryId_returnsAllGoods() {
        GoodsEntity goods1 = new GoodsEntity();
        goods1.setId(1L);
        goods1.setName("商品A");
        goods1.setImg("img1.jpg");
        goods1.setSellPrice(new BigDecimal("99.99"));
        goods1.setSale(100);
        goods1.setStoreNums(50);
        goods1.setIsDel(GoodsEntity.STATUS_ON_SALE);

        Page<GoodsEntity> entityPage = new Page<>(1, 10, 1);
        entityPage.setRecords(Collections.singletonList(goods1));

        when(goodsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(entityPage);

        CategoryExtendEntity extend = new CategoryExtendEntity();
        extend.setGoodsId(1L);
        extend.setCategoryId(1L);
        when(categoryExtendMapper.selectList(null)).thenReturn(Collections.singletonList(extend));

        CategoryEntity category = new CategoryEntity();
        category.setId(1L);
        category.setName("电子产品");
        when(categoryMapper.selectList(null)).thenReturn(Collections.singletonList(category));

        Page<GoodsVO> result = goodsService.getGoodsList(0L, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals("商品A", result.getRecords().get(0).getName());
        assertEquals("电子产品", result.getRecords().get(0).getCategoryName());
    }

    @Test
    @DisplayName("getGoodsList - 指定分类返回该分类商品")
    void getGoodsList_withCategoryId_returnsFilteredGoods() {
        CategoryExtendEntity extend = new CategoryExtendEntity();
        extend.setGoodsId(1L);
        extend.setCategoryId(5L);
        when(categoryExtendMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(extend));

        GoodsEntity goods = new GoodsEntity();
        goods.setId(1L);
        goods.setName("分类商品");
        goods.setImg("img.jpg");
        goods.setSellPrice(new BigDecimal("59.99"));
        goods.setSale(50);
        goods.setStoreNums(20);
        goods.setIsDel(GoodsEntity.STATUS_ON_SALE);

        Page<GoodsEntity> entityPage = new Page<>(1, 10, 1);
        entityPage.setRecords(Collections.singletonList(goods));
        when(goodsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(entityPage);

        CategoryEntity category = new CategoryEntity();
        category.setId(5L);
        category.setName("服装");
        when(categoryMapper.selectList(null)).thenReturn(Collections.singletonList(category));
        when(categoryExtendMapper.selectList(null)).thenReturn(Collections.singletonList(extend));

        Page<GoodsVO> result = goodsService.getGoodsList(5L, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals("分类商品", result.getRecords().get(0).getName());
    }

    @Test
    @DisplayName("getGoodsList - 分类下无商品返回空结果")
    void getGoodsList_noGoodsInCategory_returnsEmptyPage() {
        when(categoryExtendMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        Page<GoodsVO> result = goodsService.getGoodsList(5L, 1, 10);

        assertNotNull(result);
        assertEquals(0, result.getRecords().size());
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("getCategoryList - 返回顶级分类列表")
    void getCategoryList_returnsTopLevelCategories() {
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

        when(categoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(cat1, cat2));

        List<CategoryEntity> result = goodsService.getCategoryList();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("电子产品", result.get(0).getName());
        assertEquals("服装", result.get(1).getName());
    }

    @Test
    @DisplayName("getCategoryList - 无分类返回空列表")
    void getCategoryList_noCategories_returnsEmptyList() {
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<CategoryEntity> result = goodsService.getCategoryList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    }
