package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_prop")
public class PropEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String cardName;
    private String cardPwd;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal value;
    private Integer type;
    private String condition;
    private Integer isClose;
    private String img;
    private Integer isUserd;
    private Integer isSend;
    private Integer sellerId;
}