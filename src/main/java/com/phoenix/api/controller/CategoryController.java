package com.phoenix.api.controller;

import com.phoenix.api.entity.CategoryEntity;
import com.phoenix.api.result.Result;
import com.phoenix.api.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final GoodsService goodsService;

    /**
     * 获取顶级分类列表（前端展示用）
     */
    @GetMapping("/list")
    public Result<List<CategoryEntity>> list() {
        List<CategoryEntity> categories = goodsService.getCategoryList();
        return Result.success(categories);
    }

    /**
     * 获取全部分类列表（后台管理用）
     */
    @GetMapping("/all")
    public Result<List<CategoryEntity>> all() {
        List<CategoryEntity> categories = goodsService.getAllCategoryList();
        return Result.success(categories);
    }

    /**
     * 保存分类（新增/编辑）
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody CategoryEntity category) {
        goodsService.saveCategory(category);
        return Result.success("保存成功");
    }

    /**
     * 删除分类
     */
    @PostMapping("/del")
    public Result<String> delete(@RequestParam Long id) {
        goodsService.deleteCategory(id);
        return Result.success("删除成功");
    }
}
