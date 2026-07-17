package com.phoenix.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phoenix.api.entity.AccountLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountLogMapper extends BaseMapper<AccountLogEntity> {
}