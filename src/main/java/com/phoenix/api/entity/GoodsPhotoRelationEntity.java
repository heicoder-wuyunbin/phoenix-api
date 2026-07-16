package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_goods_photo_relation")
public class GoodsPhotoRelationEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private String photoId;
}