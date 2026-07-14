package com.phoenix.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phoenix.api.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    @Select("SELECT u.id, u.username, u.password, u.head_ico, m.status " +
            "FROM \"user\" u JOIN member m ON u.id = m.user_id " +
            "WHERE (u.username = #{loginInfo} OR m.email = #{loginInfo} OR m.mobile = #{loginInfo}) " +
            "AND m.status = 1")
    UserEntity findByLoginInfo(@Param("loginInfo") String loginInfo);
}