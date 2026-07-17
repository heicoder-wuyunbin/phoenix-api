package com.phoenix.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_address")
public class AddressEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String acceptName;
    private String zip;
    private String telphone;
    private Integer country;
    private Integer province;
    private Integer city;
    private Integer area;
    private String address;
    private String mobile;
    private Boolean isDefault;
}