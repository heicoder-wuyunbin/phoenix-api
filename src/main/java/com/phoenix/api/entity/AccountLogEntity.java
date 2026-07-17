package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_account_log")
public class AccountLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long adminId;
    private Long userId;
    private Integer type;
    private Integer event;
    private LocalDateTime time;
    private BigDecimal amount;
    private BigDecimal amountLog;
    private String note;
}