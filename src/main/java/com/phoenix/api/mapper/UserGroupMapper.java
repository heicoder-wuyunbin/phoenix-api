package com.phoenix.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phoenix.api.entity.UserGroupEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserGroupMapper extends BaseMapper<UserGroupEntity> {
    @Select("SELECT id FROM user_group WHERE #{exp} BETWEEN minexp AND maxexp AND minexp > 0 AND maxexp > 0 ORDER BY discount DESC LIMIT 1")
    Long findGroupIdByExp(@Param("exp") Integer exp);
}