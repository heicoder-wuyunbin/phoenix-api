package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_goods")
public class GoodsEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
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
    @TableLogic(value = "false", delval = "true")
    private Boolean isDel;
}
