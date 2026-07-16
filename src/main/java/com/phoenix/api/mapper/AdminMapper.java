package com.phoenix.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phoenix.api.entity.AdminEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminMapper extends BaseMapper<AdminEntity> {
    @Select("SELECT a.*, r.name AS role_name FROM tb_admin a LEFT JOIN tb_admin_role r ON a.role_id = r.id WHERE a.admin_name = #{adminName} AND a.is_del = false")
    AdminEntity findByAdminName(@Param("adminName") String adminName);
}