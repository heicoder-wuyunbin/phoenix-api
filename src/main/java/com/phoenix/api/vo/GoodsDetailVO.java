package com.phoenix.api.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsDetailVO {
    private Long id;
    private String name;
    private String image;
    private List<String> images;
    private BigDecimal price;
    private BigDecimal marketPrice;
    private Integer stock;
    private Integer sales;
    private String categoryName;
    private String description;
    private String content;
    private Boolean hasSpec;
    private List<SpecVO> specs;
    private List<SpecTreeVO> specTree;
    private List<SkuVO> skuList;

    @Data
    public static class SpecVO {
        private Long id;
        private String name;
        private List<String> values;
    }
}
