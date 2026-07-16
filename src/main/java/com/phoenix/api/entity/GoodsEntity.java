package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_goods")
public class GoodsEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String goodsNo;
    private String img;
    private BigDecimal sellPrice;
    private BigDecimal marketPrice;
    private BigDecimal costPrice;
    private Integer storeNums;
    private Integer sale;
    private Integer sort;
    private String keywords;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime upTime;
    private LocalDateTime downTime;
    private Integer isDel;

    public static final Integer STATUS_ON_SALE = 0;
    public static final Integer STATUS_DELETED = 1;
    public static final Integer STATUS_OFF_SALE = 2;
    public static final Integer STATUS_PENDING_REVIEW = 3;
}
