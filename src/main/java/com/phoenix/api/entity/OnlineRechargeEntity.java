package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_online_recharge")
public class OnlineRechargeEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String rechargeNo;
    private BigDecimal account;
    private LocalDateTime time;
    private String paymentName;
    private Integer status;
}