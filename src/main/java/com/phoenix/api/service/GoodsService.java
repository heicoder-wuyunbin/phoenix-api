package com.phoenix.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phoenix.api.dto.GoodsAddDTO;
import com.phoenix.api.entity.CategoryEntity;
import com.phoenix.api.vo.GoodsDetailVO;
import com.phoenix.api.vo.GoodsVO;

import java.util.List;

public interface GoodsService {

    Page<GoodsVO> getGoodsList(Long categoryId, Integer pageNum, Integer pageSize);

    List<CategoryEntity> getCategoryList();

    /**
     * 获取商品详情
     * @param id 商品ID
     * @return 商品详情（含规格树和SKU列表）
     */
    GoodsDetailVO getGoodsDetail(Long id);

    /**
     * 新增商品
     * @param dto 商品信息
     * @return 是否成功
     */
    boolean addGoods(GoodsAddDTO dto);

    /**
     * 商品上架
     * @param id 商品ID
     * @return 是否成功
     */
    boolean putOnSale(Long id);

    /**
     * 商品下架
     * @param id 商品ID
     * @return 是否成功
     */
    boolean putOffSale(Long id);
}
