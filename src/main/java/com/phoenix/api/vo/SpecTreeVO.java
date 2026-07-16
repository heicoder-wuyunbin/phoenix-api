package com.phoenix.api.vo;

import lombok.Data;

import java.util.List;

/**
 * 规格树 VO
 * 对应原代码：./old/iwebshop/controllers/goods.php 中 detail 方法的 specTree 构建逻辑
 */
@Data
public class SpecTreeVO {
    private String name;
    private List<String> options;
}