package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_comment")
public class CommentEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private String orderNo;
    private Long userId;
    private LocalDateTime time;
    private LocalDateTime commentTime;
    private String contents;
    private String recontents;
    private LocalDateTime recommentTime;
    private Integer point;
    private Integer status;
    private Integer sellerId;
}