package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_ticket")
public class TicketEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private BigDecimal value;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer point;
    private Integer sellerId;
}