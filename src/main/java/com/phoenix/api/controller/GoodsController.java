package com.phoenix.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phoenix.api.dto.GoodsAddDTO;
import com.phoenix.api.entity.CategoryEntity;
import com.phoenix.api.result.Result;
import com.phoenix.api.service.GoodsService;
import com.phoenix.api.vo.GoodsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    /**
     * 获取商品分类列表
     * @return 分类列表
     */
    @GetMapping("/categories")
    public Result<List<CategoryEntity>> categories() {
        List<CategoryEntity> list = goodsService.getCategoryList();
        return Result.success(list);
    }

    @GetMapping("/list")
    public Result<Page<GoodsVO>> list(
            @RequestParam(defaultValue = "0") Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<GoodsVO> page = goodsService.getGoodsList(categoryId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 新增商品
     * @param dto 商品信息
     * @return 操作结果
     */
    @PostMapping
    public Result<String> add(@Valid @RequestBody GoodsAddDTO dto) {
        boolean success = goodsService.addGoods(dto);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    /**
     * 商品上架
     * @param id 商品ID
     * @return 操作结果
     */
    @PutMapping("/{id}/on")
    public Result<String> putOnSale(@PathVariable Long id) {
        boolean success = goodsService.putOnSale(id);
        return success ? Result.success("上架成功") : Result.error("上架失败");
    }

    /**
     * 商品下架
     * @param id 商品ID
     * @return 操作结果
     */
    @PutMapping("/{id}/off")
    public Result<String> putOffSale(@PathVariable Long id) {
        boolean success = goodsService.putOffSale(id);
        return success ? Result.success("下架成功") : Result.error("下架失败");
    }
}
