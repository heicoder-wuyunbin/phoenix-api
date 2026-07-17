package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_favorite")
public class FavoriteEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long rid;
    private LocalDateTime time;
    private String summary;
    private Integer catId;
}