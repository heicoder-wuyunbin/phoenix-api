package com.phoenix.api.controller;

import com.phoenix.api.result.Result;
import com.phoenix.api.service.PropService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/prop")
@RequiredArgsConstructor
public class PropController {

    private final PropService propService;

    /**
     * TODO: 获取代金券列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getList(@RequestAttribute Long userId,
                                               @RequestParam(required = false) String status,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.badRequest("TODO");
    }
}