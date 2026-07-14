package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_category_extend")
public class CategoryExtendEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private Long categoryId;
}
