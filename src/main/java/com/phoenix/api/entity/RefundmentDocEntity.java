package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_refundment_doc")
public class RefundmentDocEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private LocalDateTime time;
    private Long adminId;
    private Integer payStatus;
    private String content;
    private LocalDateTime disposeTime;
    private String disposeIdea;
    private Boolean ifDel;
    private String orderGoodsId;
    private Integer sellerId;
    private String way;
}