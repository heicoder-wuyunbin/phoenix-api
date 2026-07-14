package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_user_group")
public class UserGroupEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer minexp;
    private Integer maxexp;
    private Double discount;
}