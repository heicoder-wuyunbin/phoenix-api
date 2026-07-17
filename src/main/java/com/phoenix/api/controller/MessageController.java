package com.phoenix.api.controller;

import com.phoenix.api.result.Result;
import com.phoenix.api.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * TODO: 获取消息列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getList(@RequestAttribute Long userId,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 阅读消息
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> read(@RequestAttribute Long userId, @PathVariable Long id) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 删除消息
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@RequestAttribute Long userId, @PathVariable Long id) {
        return Result.badRequest("TODO");
    }
}