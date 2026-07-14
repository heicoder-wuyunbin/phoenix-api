package com.phoenix.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phoenix.api.entity.MemberEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MemberMapper extends BaseMapper<MemberEntity> {
    @Select("SELECT * FROM member WHERE email = #{email}")
    MemberEntity findByEmail(@Param("email") String email);

    @Select("SELECT * FROM member WHERE mobile = #{mobile}")
    MemberEntity findByMobile(@Param("mobile") String mobile);

    @Select("SELECT m.*, u.head_ico FROM member m JOIN \"user\" u ON m.user_id = u.id WHERE m.user_id = #{userId}")
    MemberEntity findByUserIdWithHeadIco(@Param("userId") Long userId);
}