package com.phoenix.api.controller;

import com.phoenix.api.result.Result;
import com.phoenix.api.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    /**
     * TODO: 获取评价列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getList(@RequestAttribute Long userId,
                                               @RequestParam(required = false) String type,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 发表评价
     */
    @PostMapping
    public Result<Void> add(@RequestAttribute Long userId, @RequestBody Map<String, Object> params) {
        return Result.badRequest("TODO");
    }
}