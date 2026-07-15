package com.phoenix.api.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车项 VO
 */
@Data
public class CartItemVO {
    private Long skuId;
    private String name;
    private String image;
    private String specText;
    private BigDecimal price;
    private Integer quantity;
    private Integer stock;
}
