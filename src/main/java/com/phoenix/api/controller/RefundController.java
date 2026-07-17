package com.phoenix.api.controller;

import com.phoenix.api.result.Result;
import com.phoenix.api.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/refund")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    /**
     * TODO: 获取退款列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getList(@RequestAttribute Long userId,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 提交退款申请
     */
    @PostMapping
    public Result<Integer> add(@RequestAttribute Long userId, @RequestBody Map<String, Object> params) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 获取退款详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@RequestAttribute Long userId, @PathVariable Long id) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 取消退款申请
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestAttribute Long userId, @PathVariable Long id) {
        return Result.badRequest("TODO");
    }
}