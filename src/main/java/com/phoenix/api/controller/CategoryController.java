package com.phoenix.api.controller;

import com.phoenix.api.entity.CategoryEntity;
import com.phoenix.api.result.Result;
import com.phoenix.api.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final GoodsService goodsService;

    @GetMapping("/list")
    public Result<List<CategoryEntity>> list() {
        List<CategoryEntity> categories = goodsService.getCategoryList();
        return Result.success(categories);
    }
}
