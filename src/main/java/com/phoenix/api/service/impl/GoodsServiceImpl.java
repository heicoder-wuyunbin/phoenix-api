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
import com.phoenix.api.entity.GoodsPhotoEntity;
import com.phoenix.api.entity.GoodsPhotoRelationEntity;
import com.phoenix.api.entity.ProductEntity;
import com.phoenix.api.exception.BusinessException;
import com.phoenix.api.mapper.CategoryExtendMapper;
import com.phoenix.api.mapper.CategoryMapper;
import com.phoenix.api.mapper.GoodsMapper;
import com.phoenix.api.mapper.GoodsPhotoMapper;
import com.phoenix.api.mapper.GoodsPhotoRelationMapper;
import com.phoenix.api.mapper.ProductMapper;
import com.phoenix.api.service.GoodsService;
import com.phoenix.api.vo.GoodsDetailVO;
import com.phoenix.api.vo.GoodsVO;
import com.phoenix.api.vo.SkuVO;
import com.phoenix.api.vo.SpecTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
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
    private final GoodsPhotoMapper goodsPhotoMapper;
    private final GoodsPhotoRelationMapper goodsPhotoRelationMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Page<GoodsVO> getGoodsList(Long categoryId, Integer pageNum, Integer pageSize) {
        return getGoodsList(categoryId, pageNum, pageSize, null);
    }

    @Override
    public Page<GoodsVO> getGoodsList(Long categoryId, Integer pageNum, Integer pageSize, Integer status) {
        LambdaQueryWrapper<GoodsEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (status == null) {
            wrapper.eq(GoodsEntity::getIsDel, GoodsEntity.STATUS_ON_SALE);
        } else {
            wrapper.eq(GoodsEntity::getIsDel, status);
        }
        
        if (categoryId != null && categoryId != 0) {
            List<CategoryExtendEntity> categoryExtends = categoryExtendMapper.selectList(
                new LambdaQueryWrapper<CategoryExtendEntity>()
                    .eq(CategoryExtendEntity::getCategoryId, categoryId)
            );
            List<Long> goodsIds = categoryExtends.stream()
                .map(CategoryExtendEntity::getGoodsId)
                .collect(Collectors.toList());
            
            if (goodsIds.isEmpty()) {
                return new Page<>(pageNum, pageSize, 0);
            }
            
            wrapper.in(GoodsEntity::getId, goodsIds);
        }
        
        wrapper.orderByDesc(GoodsEntity::getSale);
        
        Page<GoodsEntity> entityPage = new Page<>(pageNum, pageSize);
        Page<GoodsEntity> goodsPage = goodsMapper.selectPage(entityPage, wrapper);
        
        List<CategoryExtendEntity> allCategoryExtends = categoryExtendMapper.selectList(null);
        Map<Long, Long> goodsToCategoryMap = allCategoryExtends.stream()
                .collect(Collectors.toMap(
                        CategoryExtendEntity::getGoodsId,
                        CategoryExtendEntity::getCategoryId,
                        (existing, replacement) -> existing
                ));
        
        List<CategoryEntity> allCategories = categoryMapper.selectList(null);
        Map<Long, String> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(CategoryEntity::getId, CategoryEntity::getName));
        
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
                    vo.setIsDel(entity.getIsDel() != null && entity.getIsDel() != 0);
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
    public List<CategoryEntity> getAllCategoryList() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<CategoryEntity>()
                        .orderByAsc(CategoryEntity::getSort)
        );
    }

    @Override
    public void saveCategory(CategoryEntity category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new BusinessException("分类名称不能为空");
        }
        if (category.getParentId() == null) {
            category.setParentId(0);
        }
        if (category.getSort() == null) {
            category.setSort(99);
        }
        if (category.getId() != null && category.getId() > 0) {
            categoryMapper.updateById(category);
        } else {
            category.setId(null);
            categoryMapper.insert(category);
        }
    }

    @Override
    public void deleteCategory(Long id) {
        // 检查是否有子分类
        Long subCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<CategoryEntity>()
                        .eq(CategoryEntity::getParentId, id)
        );
        if (subCount > 0) {
            throw new BusinessException("无法删除此分类，此分类下还有子分类");
        }
        // 删除分类关联
        categoryExtendMapper.delete(
                new LambdaQueryWrapper<CategoryExtendEntity>()
                        .eq(CategoryExtendEntity::getCategoryId, id)
        );
        categoryMapper.deleteById(id);
    }

    @Override
    public GoodsDetailVO getGoodsDetail(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("商品不存在");
        }
        GoodsEntity goods = goodsMapper.selectById(id);
        if (goods == null || GoodsEntity.STATUS_DELETED.equals(goods.getIsDel())) {
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
        entity.setGoodsNo(generateGoodsNo());
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
        entity.setIsDel(GoodsEntity.STATUS_ON_SALE);

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
        if (id == null || id <= 0) {
            throw new BusinessException("上架失败");
        }
        GoodsEntity goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        LambdaUpdateWrapper<GoodsEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(GoodsEntity::getId, id)
                .set(GoodsEntity::getIsDel, GoodsEntity.STATUS_ON_SALE)
                .set(GoodsEntity::getUpTime, LocalDateTime.now())
                .set(GoodsEntity::getDownTime, null);
        return goodsMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean putOffSale(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("下架失败");
        }
        GoodsEntity goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        LambdaUpdateWrapper<GoodsEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(GoodsEntity::getId, id)
                .set(GoodsEntity::getIsDel, GoodsEntity.STATUS_OFF_SALE)
                .set(GoodsEntity::getUpTime, null)
                .set(GoodsEntity::getDownTime, LocalDateTime.now());
        return goodsMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean submitForReview(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("操作失败");
        }
        GoodsEntity goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        LambdaUpdateWrapper<GoodsEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(GoodsEntity::getId, id)
                .set(GoodsEntity::getIsDel, GoodsEntity.STATUS_PENDING_REVIEW)
                .set(GoodsEntity::getUpTime, null)
                .set(GoodsEntity::getDownTime, null);
        return goodsMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean softDelete(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("删除失败");
        }
        GoodsEntity goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        LambdaUpdateWrapper<GoodsEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(GoodsEntity::getId, id)
                .set(GoodsEntity::getIsDel, GoodsEntity.STATUS_DELETED)
                .set(GoodsEntity::getUpTime, null)
                .set(GoodsEntity::getDownTime, null);
        return goodsMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean restore(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("还原失败");
        }
        GoodsEntity goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        LambdaUpdateWrapper<GoodsEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(GoodsEntity::getId, id)
                .set(GoodsEntity::getIsDel, GoodsEntity.STATUS_ON_SALE)
                .set(GoodsEntity::getUpTime, LocalDateTime.now())
                .set(GoodsEntity::getDownTime, null);
        return goodsMapper.update(null, wrapper) > 0;
    }

    @Override
    @Transactional
    public boolean hardDelete(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("删除失败");
        }
        GoodsEntity goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }

        List<GoodsPhotoRelationEntity> photoRelations = goodsPhotoRelationMapper.selectList(
                new LambdaQueryWrapper<GoodsPhotoRelationEntity>()
                        .eq(GoodsPhotoRelationEntity::getGoodsId, id)
        );

        for (GoodsPhotoRelationEntity relation : photoRelations) {
            Long count = goodsPhotoRelationMapper.selectCount(
                    new LambdaQueryWrapper<GoodsPhotoRelationEntity>()
                            .eq(GoodsPhotoRelationEntity::getPhotoId, relation.getPhotoId())
                            .ne(GoodsPhotoRelationEntity::getGoodsId, id)
            );
            if (count == 0) {
                GoodsPhotoEntity photo = goodsPhotoMapper.selectById(relation.getPhotoId());
                if (photo != null && photo.getImg() != null) {
                    File imgFile = new File(photo.getImg());
                    if (imgFile.exists()) {
                        imgFile.delete();
                    }
                }
                goodsPhotoMapper.deleteById(relation.getPhotoId());
            }
        }

        goodsPhotoRelationMapper.delete(
                new LambdaQueryWrapper<GoodsPhotoRelationEntity>()
                        .eq(GoodsPhotoRelationEntity::getGoodsId, id)
        );

        goodsMapper.deleteById(id);
        return true;
    }

    @Override
    public boolean batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要操作的商品");
        }
        LambdaUpdateWrapper<GoodsEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(GoodsEntity::getId, ids);

        if (GoodsEntity.STATUS_ON_SALE.equals(status)) {
            wrapper.set(GoodsEntity::getIsDel, GoodsEntity.STATUS_ON_SALE)
                    .set(GoodsEntity::getUpTime, LocalDateTime.now())
                    .set(GoodsEntity::getDownTime, null);
        } else if (GoodsEntity.STATUS_OFF_SALE.equals(status)) {
            wrapper.set(GoodsEntity::getIsDel, GoodsEntity.STATUS_OFF_SALE)
                    .set(GoodsEntity::getUpTime, null)
                    .set(GoodsEntity::getDownTime, LocalDateTime.now());
        } else if (GoodsEntity.STATUS_PENDING_REVIEW.equals(status)) {
            wrapper.set(GoodsEntity::getIsDel, GoodsEntity.STATUS_PENDING_REVIEW)
                    .set(GoodsEntity::getUpTime, null)
                    .set(GoodsEntity::getDownTime, null);
        } else if (GoodsEntity.STATUS_DELETED.equals(status)) {
            wrapper.set(GoodsEntity::getIsDel, GoodsEntity.STATUS_DELETED)
                    .set(GoodsEntity::getUpTime, null)
                    .set(GoodsEntity::getDownTime, null);
        } else {
            throw new BusinessException("无效的状态值");
        }

        return goodsMapper.update(null, wrapper) > 0;
    }

    private String generateGoodsNo() {
        return "G" + System.currentTimeMillis() + new Random().nextInt(90) + 10;
    }
}
