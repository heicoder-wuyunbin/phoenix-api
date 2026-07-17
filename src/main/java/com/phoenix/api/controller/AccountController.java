package com.phoenix.api.controller;

import com.phoenix.api.result.Result;
import com.phoenix.api.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * TODO: 获取余额交易记录
     */
    @GetMapping("/log")
    public Result<Map<String, Object>> getLog(@RequestAttribute Long userId,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 提交充值申请
     */
    @PostMapping("/recharge")
    public Result<String> recharge(@RequestAttribute Long userId, @RequestBody Map<String, Object> params) {
        return Result.badRequest("TODO");
    }
}