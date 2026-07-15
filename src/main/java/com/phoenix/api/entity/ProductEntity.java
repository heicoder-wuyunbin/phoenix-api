package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 货品表（SKU）
 * 对应数据库：tb_products
 */
@Data
@TableName("tb_products")
public class ProductEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private String productsNo;
    private String specArray;
    private Integer storeNums;
    private BigDecimal marketPrice;
    private BigDecimal sellPrice;
    private BigDecimal costPrice;
    private BigDecimal weight;
    private String img;
    @TableLogic(value = "false", delval = "true")
    private Boolean isDel;
}
