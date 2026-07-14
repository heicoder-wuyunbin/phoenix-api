package com.phoenix.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phoenix.api.result.Result;
import com.phoenix.api.service.GoodsService;
import com.phoenix.api.vo.GoodsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    @GetMapping("/list")
    public Result<Page<GoodsVO>> list(
            @RequestParam(defaultValue = "0") Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<GoodsVO> page = goodsService.getGoodsList(categoryId, pageNum, pageSize);
        return Result.success(page);
    }
}
