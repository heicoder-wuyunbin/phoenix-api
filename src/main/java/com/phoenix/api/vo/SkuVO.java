package com.phoenix.api.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * SKU VO
 * 对应原代码：./old/iwebshop/controllers/goods.php 中 detail 方法的 skuList 构建逻辑
 */
@Data
public class SkuVO {
    private Long id;
    private Map<String, String> spec;
    private BigDecimal price;
    private Integer stock;
    private String image;
}