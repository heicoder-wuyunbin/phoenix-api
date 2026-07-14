package com.phoenix.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phoenix.api.entity.CategoryEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity> {
}
