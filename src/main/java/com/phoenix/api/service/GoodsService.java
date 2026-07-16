package com.phoenix.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phoenix.api.dto.GoodsAddDTO;
import com.phoenix.api.entity.CategoryEntity;
import com.phoenix.api.vo.GoodsDetailVO;
import com.phoenix.api.vo.GoodsVO;

import java.util.List;

public interface GoodsService {

    Page<GoodsVO> getGoodsList(Long categoryId, Integer pageNum, Integer pageSize);

    Page<GoodsVO> getGoodsList(Long categoryId, Integer pageNum, Integer pageSize, Integer status);

    List<CategoryEntity> getCategoryList();

    List<CategoryEntity> getAllCategoryList();

    void saveCategory(CategoryEntity category);

    void deleteCategory(Long id);

    GoodsDetailVO getGoodsDetail(Long id);

    boolean addGoods(GoodsAddDTO dto);

    boolean putOnSale(Long id);

    boolean putOffSale(Long id);

    /**
     * 提交审核
     * @param id 商品ID
     * @return 是否成功
     */
    boolean submitForReview(Long id);

    /**
     * 软删除（移到回收站）
     * @param id 商品ID
     * @return 是否成功
     */
    boolean softDelete(Long id);

    /**
     * 从回收站还原
     * @param id 商品ID
     * @return 是否成功
     */
    boolean restore(Long id);

    /**
     * 彻底删除（物理删除）
     * @param id 商品ID
     * @return 是否成功
     */
    boolean hardDelete(Long id);

    /**
     * 批量更新商品状态
     * @param ids 商品ID列表
     * @param status 状态值(0=上架,1=删除,2=下架,3=待审)
     * @return 是否成功
     */
    boolean batchUpdateStatus(List<Long> ids, Integer status);
}
