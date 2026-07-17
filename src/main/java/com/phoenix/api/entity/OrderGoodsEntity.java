package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("tb_order_goods")
public class OrderGoodsEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long goodsId;
    private String img;
    private Integer productId;
    private BigDecimal goodsPrice;
    private BigDecimal realPrice;
    private Integer goodsNums;
    private BigDecimal goodsWeight;
    private String goodsArray;
    private Integer isSend;
    private Integer sellerId;
}