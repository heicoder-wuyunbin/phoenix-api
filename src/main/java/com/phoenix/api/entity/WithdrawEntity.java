package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_withdraw")
public class WithdrawEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDateTime time;
    private BigDecimal amount;
    private String name;
    private Integer status;
    private String note;
    private String reNote;
    private Boolean isDel;
}