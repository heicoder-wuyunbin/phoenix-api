package com.phoenix.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsAddDTO {
    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String img;

    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于0")
    private BigDecimal sellPrice;

    @DecimalMin(value = "0", message = "市场价不能小于0")
    private BigDecimal marketPrice;

    @DecimalMin(value = "0", message = "成本价不能小于0")
    private BigDecimal costPrice;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer storeNums;

    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

    private String keywords;

    private String content;

    private Long categoryId;
}
