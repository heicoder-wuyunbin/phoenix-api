package com.phoenix.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phoenix.api.entity.CategoryEntity;
import com.phoenix.api.vo.GoodsVO;

import java.util.List;

public interface GoodsService {

    Page<GoodsVO> getGoodsList(Long categoryId, Integer pageNum, Integer pageSize);

    List<CategoryEntity> getCategoryList();
}
