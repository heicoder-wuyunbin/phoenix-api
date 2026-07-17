package com.phoenix.api.controller;

import com.phoenix.api.result.Result;
import com.phoenix.api.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * TODO: 获取地址列表
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getList(@RequestAttribute Long userId) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 新增地址
     */
    @PostMapping
    public Result<Integer> add(@RequestAttribute Long userId, @RequestBody Map<String, Object> params) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 修改地址
     */
    @PutMapping("/{id}")
    public Result<Void> update(@RequestAttribute Long userId, @PathVariable Long id, @RequestBody Map<String, Object> params) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 删除地址
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestAttribute Long userId, @PathVariable Long id) {
        return Result.badRequest("TODO");
    }

    /**
     * TODO: 设置默认地址
     */
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@RequestAttribute Long userId, @PathVariable Long id) {
        return Result.badRequest("TODO");
    }
}