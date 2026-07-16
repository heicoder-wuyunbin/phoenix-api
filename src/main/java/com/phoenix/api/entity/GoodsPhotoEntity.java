package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_goods_photo")
public class GoodsPhotoEntity {
    private String id;
    private String img;
}