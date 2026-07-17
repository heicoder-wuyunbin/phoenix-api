package com.phoenix.api.controller;

import com.phoenix.api.result.Result;
import com.phoenix.api.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * TODO: 获取收藏列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getList(@RequestAttribute Long userId,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 取消收藏
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestAttribute Long userId, @PathVariable Long id) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 编辑收藏备注
     */
    @PutMapping("/{id}/summary")
    public Result<Void> updateSummary(@RequestAttribute Long userId, @PathVariable Long id, @RequestBody Map<String, Object> params) {
        return Result.badRequest("TODO");
    }
}