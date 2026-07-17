package com.phoenix.api.controller;

import com.phoenix.api.result.Result;
import com.phoenix.api.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * TODO: 获取用户中心首页数据
     */
    @GetMapping("/center")
    public Result<Map<String, Object>> center(@RequestAttribute Long userId) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 获取个人资料
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> profile(@RequestAttribute Long userId) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 修改个人资料
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestAttribute Long userId, @RequestBody Map<String, Object> params) {
        return Result.badRequest("TODO");
    }
}