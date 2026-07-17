package com.phoenix.api.controller;

import com.phoenix.api.result.Result;
import com.phoenix.api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * TODO: 获取订单列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getList(@RequestAttribute Long userId,
                                               @RequestParam(required = false) String status,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@RequestAttribute Long userId, @PathVariable Long id) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 取消订单
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@RequestAttribute Long userId, @PathVariable Long id) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 确认收货
     */
    @PutMapping("/{id}/confirm")
    public Result<Void> confirm(@RequestAttribute Long userId, @PathVariable Long id) {
        return Result.badRequest("TODO");
    }
}