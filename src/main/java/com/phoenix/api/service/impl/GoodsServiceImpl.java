package com.phoenix.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenix.api.dto.GoodsAddDTO;
import com.phoenix.api.entity.CategoryEntity;
import com.phoenix.api.entity.CategoryExtendEntity;
import com.phoenix.api.entity.GoodsEntity;
import com.phoenix.api.entity.ProductEntity;
import com.phoenix.api.exception.BusinessException;
import com.phoenix.api.mapper.CategoryExtendMapper;
import com.phoenix.api.mapper.CategoryMapper;
import com.phoenix.api.mapper.GoodsMapper;
import com.phoenix.api.mapper.ProductMapper;
import com.phoenix.api.service.GoodsService;
import com.phoenix.api.vo.GoodsDetailVO;
import com.phoenix.api.vo.GoodsVO;
import com.phoenix.api.vo.SkuVO;
import com.phoenix.api.vo.SpecTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodsServiceImpl implements GoodsService {

    private final GoodsMapper goodsMapper;
    private final CategoryMapper categoryMapper;
    private final CategoryExtendMapper categoryExtendMapper;
    private final ProductMapper productMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Page<GoodsVO> getGoodsList(Long categoryId, Integer pageNum, Integer pageSize) {
        // 构建查询条件
        LambdaQueryWrapper<GoodsEntity> wrapper = new LambdaQueryWrapper<>();
        
        // 如果指定了分类，需要通过 tb_category_extend 表关联查询
        if (categoryId != null && categoryId != 0) {
            // 先查询该分类下的所有商品 ID
            List<CategoryExtendEntity> categoryExtends = categoryExtendMapper.selectList(
                new LambdaQueryWrapper<CategoryExtendEntity>()
                    .eq(CategoryExtendEntity::getCategoryId, categoryId)
            );
            List<Long> goodsIds = categoryExtends.stream()
                .map(CategoryExtendEntity::getGoodsId)
                .collect(Collectors.toList());
            
            if (goodsIds.isEmpty()) {
                // 该分类下没有商品，返回空结果
                return new Page<>(pageNum, pageSize, 0);
            }
            
            wrapper.in(GoodsEntity::getId, goodsIds);
        }
        
        // 按销量降序排序
        wrapper.orderByDesc(GoodsEntity::getSale);
        
        // 使用内置方法分页查询
        Page<GoodsEntity> entityPage = new Page<>(pageNum, pageSize);
        Page<GoodsEntity> goodsPage = goodsMapper.selectPage(entityPage, wrapper);
        
        // 获取所有分类关联信息
        List<CategoryExtendEntity> allCategoryExtends = categoryExtendMapper.selectList(null);
        Map<Long, Long> goodsToCategoryMap = allCategoryExtends.stream()
                .collect(Collectors.toMap(CategoryExtendEntity::getGoodsId, CategoryExtendEntity::getCategoryId));
        
        // 获取所有分类信息用于转换
        List<CategoryEntity> allCategories = categoryMapper.selectList(null);
        Map<Long, String> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(CategoryEntity::getId, CategoryEntity::getName));
        
        // 转换为 VO
        Page<GoodsVO> voPage = new Page<>(goodsPage.getCurrent(), goodsPage.getSize(), goodsPage.getTotal());
        List<GoodsVO> voList = goodsPage.getRecords().stream()
                .map(entity -> {
                    GoodsVO vo = new GoodsVO();
                    vo.setId(entity.getId());
                    vo.setName(entity.getName());
                    vo.setImage(entity.getImg());
                    vo.setPrice(entity.getSellPrice());
                    vo.setSales(entity.getSale());
                    vo.setStock(entity.getStoreNums());
                    // 设置分类名称
                    Long goodsCategoryId = goodsToCategoryMap.get(entity.getId());
                    if (goodsCategoryId != null) {
                        vo.setCategoryName(categoryMap.get(goodsCategoryId));
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public List<CategoryEntity> getCategoryList() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<CategoryEntity>()
                        .eq(CategoryEntity::getParentId, 0)
                        .orderByAsc(CategoryEntity::getSort)
        );
    }

    @Override
    public GoodsDetailVO getGoodsDetail(Long id) {
        // 查询商品信息
        GoodsEntity goods = goodsMapper.selectById(id);
        if (goods == null || goods.getIsDel()) {
            throw new BusinessException("商品不存在");
        }

        // 查询关联的 Product（SKU）列表
        List<ProductEntity> products = productMapper.selectList(
                new LambdaQueryWrapper<ProductEntity>()
                        .eq(ProductEntity::getGoodsId, id)
                        .eq(ProductEntity::getIsDel, false)
        );

        // 构建规格树和 SKU 列表
        List<SpecTreeVO> specTree = buildSpecTree(products);
        List<SkuVO> skuList = buildSkuList(products);

        // 组装 GoodsDetailVO
        GoodsDetailVO vo = new GoodsDetailVO();
        vo.setId(goods.getId());
        vo.setName(goods.getName());
        vo.setImage(goods.getImg());
        vo.setPrice(goods.getSellPrice());
        vo.setMarketPrice(goods.getMarketPrice());
        vo.setStock(goods.getStoreNums());
        vo.setSales(goods.getSale());
        vo.setContent(goods.getContent());
        vo.setHasSpec(!specTree.isEmpty());
        vo.setSpecTree(specTree);
        vo.setSkuList(skuList);

        // 如果存在 SKU，使用第一个 SKU 的价格作为默认价格
        if (!skuList.isEmpty()) {
            vo.setPrice(skuList.get(0).getPrice());
            vo.setStock(products.stream().mapToInt(ProductEntity::getStoreNums).sum());
        }

        return vo;
    }

    /**
     * 构建规格树
     * 对应原代码：./old/iwebshop/controllers/goods.php 第 195-205 行
     * 使用 LinkedHashMap 保持插入顺序
     */
    private List<SpecTreeVO> buildSpecTree(List<ProductEntity> products) {
        Map<String, LinkedHashSet<String>> specMap = new LinkedHashMap<>();

        for (ProductEntity product : products) {
            Map<String, String> spec = parseSpecArray(product.getSpecArray());
            for (Map.Entry<String, String> entry : spec.entrySet()) {
                specMap.computeIfAbsent(entry.getKey(), k -> new LinkedHashSet<>())
                       .add(entry.getValue());
            }
        }

        return specMap.entrySet().stream()
                .map(entry -> {
                    SpecTreeVO vo = new SpecTreeVO();
                    vo.setName(entry.getKey());
                    vo.setOptions(new ArrayList<>(entry.getValue()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建 SKU 列表
     * 对应原代码：./old/iwebshop/controllers/goods.php 第 207-214 行
     */
    private List<SkuVO> buildSkuList(List<ProductEntity> products) {
        return products.stream()
                .map(product -> {
                    SkuVO vo = new SkuVO();
                    vo.setId(product.getId());
                    vo.setSpec(parseSpecArray(product.getSpecArray()));
                    vo.setPrice(product.getSellPrice());
                    vo.setStock(product.getStoreNums());
                    vo.setImage(product.getImg());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 解析 specArray JSON 字符串
     * 使用 Jackson 解析 JSON 格式的规格数据
     */
    private Map<String, String> parseSpecArray(String specArray) {
        if (specArray == null || specArray.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(
                    specArray,
                    new TypeReference<Map<String, String>>() {}
            );
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    @Override
    public boolean addGoods(GoodsAddDTO dto) {
        GoodsEntity entity = new GoodsEntity();
        entity.setName(dto.getName());
        entity.setImg(dto.getImg());
        entity.setSellPrice(dto.getSellPrice());
        entity.setMarketPrice(dto.getMarketPrice());
        entity.setCostPrice(dto.getCostPrice());
        entity.setStoreNums(dto.getStoreNums());
        entity.setSort(dto.getSort() != null ? dto.getSort() : 99);
        entity.setKeywords(dto.getKeywords());
        entity.setContent(dto.getContent());
        entity.setSale(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setIsDel(false);

        int rows = goodsMapper.insert(entity);
        if (rows <= 0) {
            return false;
        }

        // 如果指定了分类，建立关联关系
        if (dto.getCategoryId() != null && dto.getCategoryId() > 0) {
            CategoryExtendEntity extend = new CategoryExtendEntity();
            extend.setGoodsId(entity.getId());
            extend.setCategoryId(dto.getCategoryId());
            categoryExtendMapper.insert(extend);
        }

        return true;
    }

    @Override
    public boolean putOnSale(Long id) {
        LambdaUpdateWrapper<GoodsEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(GoodsEntity::getId, id)
                .set(GoodsEntity::getIsDel, false)
                .set(GoodsEntity::getUpTime, LocalDateTime.now())
                .set(GoodsEntity::getDownTime, null);
        return goodsMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean putOffSale(Long id) {
        LambdaUpdateWrapper<GoodsEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(GoodsEntity::getId, id)
                .set(GoodsEntity::getIsDel, true)
                .set(GoodsEntity::getUpTime, null)
                .set(GoodsEntity::getDownTime, LocalDateTime.now());
        return goodsMapper.update(null, wrapper) > 0;
    }
}
