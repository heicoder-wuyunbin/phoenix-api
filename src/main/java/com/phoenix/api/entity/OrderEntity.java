package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_order")
public class OrderEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Integer payType;
    private Integer distribution;
    private Integer status;
    private Integer payStatus;
    private Integer distributionStatus;
    private String acceptName;
    private String postcode;
    private String telphone;
    private Integer country;
    private Integer province;
    private Integer city;
    private Integer area;
    private String address;
    private String mobile;
    private BigDecimal payableAmount;
    private BigDecimal realAmount;
    private BigDecimal payableFreight;
    private BigDecimal realFreight;
    private LocalDateTime payTime;
    private LocalDateTime sendTime;
    private LocalDateTime createTime;
    private LocalDateTime completionTime;
    private String postscript;
    private String note;
    private Boolean ifDel;
    private BigDecimal orderAmount;
    private String prop;
    private String acceptTime;
    private Integer exp;
    private Integer point;
    private Integer sellerId;
    private Integer type;
}