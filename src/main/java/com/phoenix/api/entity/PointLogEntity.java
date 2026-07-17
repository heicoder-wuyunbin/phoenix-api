package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_point_log")
public class PointLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDateTime createdAt;
    private Integer value;
    private String intro;
}