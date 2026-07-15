package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_category")
public class CategoryEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer parentId;
    private Integer sort;
}
