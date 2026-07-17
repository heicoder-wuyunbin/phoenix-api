package com.phoenix.api.controller;

import com.phoenix.api.result.Result;
import com.phoenix.api.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/point")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /**
     * TODO: 获取积分记录
     */
    @GetMapping("/log")
    public Result<Map<String, Object>> getLog(@RequestAttribute Long userId,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 积分兑换代金券
     */
    @PostMapping("/exchange")
    public Result<Void> exchange(@RequestAttribute Long userId, @RequestBody Map<String, Object> params) {
        return Result.badRequest("TODO");
    }
}