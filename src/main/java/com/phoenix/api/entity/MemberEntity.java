package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("member")
public class MemberEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDateTime time;
    private Integer status;
    private String mobile;
    private String email;
    private LocalDateTime lastLogin;
    private Integer exp;
    private Integer groupId;
    @TableField(exist = false)
    private String headIco;
}