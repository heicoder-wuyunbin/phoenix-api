package com.phoenix.api.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsVO {
    private Long id;
    private String name;
    private String image;
    private BigDecimal price;
    private String categoryName;
    private Integer sales;
    private Integer stock;
}
