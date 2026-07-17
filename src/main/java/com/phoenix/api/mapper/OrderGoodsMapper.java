package com.phoenix.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phoenix.api.entity.OrderGoodsEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderGoodsMapper extends BaseMapper<OrderGoodsEntity> {
}