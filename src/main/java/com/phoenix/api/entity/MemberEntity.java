package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_member")
public class MemberEntity {
    @TableId("user_id")
    private Long userId;
    private LocalDateTime time;
    private Integer status;
    private String mobile;
    private String email;
    private LocalDateTime lastLogin;
    private Integer exp;
    private Integer groupId;
    private String trueName;
    private String telephone;
    private String area;
    private String contactAddr;
    private String qq;
    private Integer sex;
    private LocalDateTime birthday;
    private Integer point;
    private String messageIds;
    private String zip;
    private String prop;
    private BigDecimal balance;
    private String custom;
    @TableField(exist = false)
    private String headIco;
}