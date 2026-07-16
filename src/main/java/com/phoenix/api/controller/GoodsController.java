package com.phoenix.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phoenix.api.dto.GoodsAddDTO;
import com.phoenix.api.entity.CategoryEntity;
import com.phoenix.api.result.Result;
import com.phoenix.api.service.GoodsService;
import com.phoenix.api.vo.GoodsDetailVO;
import com.phoenix.api.vo.GoodsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    @GetMapping("/categories")
    public Result<List<CategoryEntity>> categories() {
        List<CategoryEntity> list = goodsService.getCategoryList();
        return Result.success(list);
    }

    @GetMapping("/detail")
    public Result<GoodsDetailVO> detail(@RequestParam Long id) {
        GoodsDetailVO detail = goodsService.getGoodsDetail(id);
        return Result.success(detail);
    }

    @GetMapping("/list")
    public Result<Page<GoodsVO>> list(
            @RequestParam(defaultValue = "0") Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<GoodsVO> page = goodsService.getGoodsList(categoryId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/admin/list")
    public Result<Page<GoodsVO>> adminList(
            @RequestParam(defaultValue = "0") Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        Page<GoodsVO> page = goodsService.getGoodsList(categoryId, pageNum, pageSize, status);
        return Result.success(page);
    }

    @PostMapping
    public Result<String> add(@Valid @RequestBody GoodsAddDTO dto) {
        boolean success = goodsService.addGoods(dto);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping("/{id}/on")
    public Result<String> putOnSale(@PathVariable Long id) {
        boolean success = goodsService.putOnSale(id);
        return success ? Result.success("上架成功") : Result.error("上架失败");
    }

    @PutMapping("/{id}/off")
    public Result<String> putOffSale(@PathVariable Long id) {
        boolean success = goodsService.putOffSale(id);
        return success ? Result.success("下架成功") : Result.error("下架失败");
    }

    @PutMapping("/{id}/review")
    public Result<String> submitForReview(@PathVariable Long id) {
        boolean success = goodsService.submitForReview(id);
        return success ? Result.success("提交审核成功") : Result.error("操作失败");
    }

    @PutMapping("/{id}/delete")
    public Result<String> softDelete(@PathVariable Long id) {
        boolean success = goodsService.softDelete(id);
        return success ? Result.success("已移到回收站") : Result.error("删除失败");
    }

    @PutMapping("/{id}/restore")
    public Result<String> restore(@PathVariable Long id) {
        boolean success = goodsService.restore(id);
        return success ? Result.success("还原成功") : Result.error("还原失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> hardDelete(@PathVariable Long id) {
        boolean success = goodsService.hardDelete(id);
        return success ? Result.success("彻底删除成功") : Result.error("删除失败");
    }

    @PutMapping("/batch/status")
    public Result<String> batchUpdateStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> ids = ((List<Number>) body.get("ids")).stream()
                .map(Number::longValue)
                .toList();
        Integer status = (Integer) body.get("status");
        boolean success = goodsService.batchUpdateStatus(ids, status);
        return success ? Result.success("批量操作成功") : Result.error("操作失败");
    }
}
