package com.phoenix.api.controller;

import com.phoenix.api.dto.CartAddDTO;
import com.phoenix.api.result.Result;
import com.phoenix.api.service.CartService;
import com.phoenix.api.vo.CartItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车 Controller
 * 对应原代码：./old/iwebshop/controllers/cart.php
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 获取当前用户 ID
     * TODO: 接入 Spring Security 后从 SecurityContext 获取
     */
    private Long getCurrentUserId() {
        // TODO: 替换为 Spring Security 的 SecurityContextHolder.getContext().getAuthentication()
        return 1L; // 默认测试用户 ID
    }

    /**
     * 添加商品到购物车
     * 对应原代码：./old/iwebshop/classes/cart.php 第 99-132 行 add() 方法
     *
     * @param dto 添加购物车请求
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<String> add(@Valid @RequestBody CartAddDTO dto) {
        cartService.addToCart(getCurrentUserId(), dto.getSkuId(), dto.getQuantity());
        return Result.success("添加成功");
    }

    /**
     * 获取购物车列表
     * 对应原代码：./old/iwebshop/classes/cart.php 第 263-267 行 getMyCart() 方法
     *
     * @return 购物车商品列表
     */
    @GetMapping("/list")
    public Result<List<CartItemVO>> list() {
        List<CartItemVO> items = cartService.getCartList(getCurrentUserId());
        return Result.success(items);
    }

    /**
     * 更新购物车商品数量
     * 对应原代码：./old/iwebshop/classes/cart.php 第 99-132 行 add() 方法的数量更新部分
     *
     * @param dto 更新购物车请求
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<String> update(@Valid @RequestBody CartAddDTO dto) {
        cartService.updateCart(getCurrentUserId(), dto.getSkuId(), dto.getQuantity());
        return Result.success("更新成功");
    }

    /**
     * 从购物车移除商品
     * 对应原代码：./old/iwebshop/classes/cart.php 第 150-169 行 del() 方法
     *
     * @param skuId SKU ID
     * @return 操作结果
     */
    @DeleteMapping("/remove")
    public Result<String> remove(@RequestParam Long skuId) {
        cartService.removeFromCart(getCurrentUserId(), skuId);
        return Result.success("移除成功");
    }

    /**
     * 清空购物车
     * 对应原代码：./old/iwebshop/classes/cart.php 第 270-287 行 clear() 方法
     *
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    public Result<String> clear() {
        cartService.clearCart(getCurrentUserId());
        return Result.success("清空成功");
    }
}