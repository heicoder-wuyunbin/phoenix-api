package com.phoenix.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenix.api.entity.GoodsEntity;
import com.phoenix.api.entity.ProductEntity;
import com.phoenix.api.exception.BusinessException;
import com.phoenix.api.mapper.GoodsMapper;
import com.phoenix.api.mapper.ProductMapper;
import com.phoenix.api.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 购物车服务
 * 对应原代码：./old/iwebshop/classes/cart.php
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CartService {

    private final StringRedisTemplate redisTemplate;
    private final ProductMapper productMapper;
    private final GoodsMapper goodsMapper;

    /**
     * Redis Key 前缀
     * 格式：phoenix:cart:{userId}
     * Hash 结构：field=skuId, value=quantity
     */
    private static final String CART_KEY_PREFIX = "phoenix:cart:";

    /**
     * 购物车过期时间：30 天
     */
    private static final long CART_EXPIRE_DAYS = 30;

    /**
     * 购物车最大商品种类数
     * 对应原代码：./old/iwebshop/classes/cart.php 第 34 行
     * private $maxCount = 100;
     */
    private static final int MAX_CART_ITEMS = 100;

    /**
     * 添加商品到购物车
     * 对应原代码：./old/iwebshop/classes/cart.php 第 99-132 行 add() 方法
     *
     * @param userId   用户ID
     * @param skuId    SKU ID（货品ID）
     * @param quantity 数量
     */
    public void addToCart(Long userId, Long skuId, Integer quantity) {
        String key = CART_KEY_PREFIX + userId;
        String skuIdStr = skuId.toString();

        // 对应原代码第 102-110 行：检查商品是否存在规格
        ProductEntity product = productMapper.selectById(skuId);
        if (product == null || product.getIsDel()) {
            throw new BusinessException("该商品不存在");
        }

        // 对应原代码第 65-69 行：检查库存
        if (product.getStoreNums() < quantity) {
            throw new BusinessException("该商品库存不足");
        }

        // 对应原代码第 115-119 行：检查购物车容量
        Long currentItemCount = redisTemplate.opsForHash().size(key);
        boolean isNewItem = redisTemplate.opsForHash().get(key, skuIdStr) == null;
        
        if (isNewItem && currentItemCount >= MAX_CART_ITEMS) {
            throw new BusinessException("加入购物车失败,购物车中最多只能容纳" + MAX_CART_ITEMS + "种商品");
        }

        // 对应原代码第 63-71 行：获取当前数量并计算新数量
        String currentQtyStr = (String) redisTemplate.opsForHash().get(key, skuIdStr);
        int currentQty = currentQtyStr != null ? Integer.parseInt(currentQtyStr) : 0;
        int newQty = currentQty + quantity;

        // 再次检查库存（累加后的数量）
        if (product.getStoreNums() < newQty) {
            throw new BusinessException("该商品库存不足");
        }

        // 更新 Redis
        redisTemplate.opsForHash().put(key, skuIdStr, String.valueOf(newQty));

        // 设置过期时间
        redisTemplate.expire(key, CART_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 获取购物车列表
     * 对应原代码：./old/iwebshop/classes/cart.php 第 263-267 行 getMyCart() 方法
     *
     * @param userId 用户ID
     * @return 购物车商品列表
     */
    public List<CartItemVO> getCartList(Long userId) {
        String key = CART_KEY_PREFIX + userId;

        // 获取购物车中所有 SKU
        Map<Object, Object> cartMap = redisTemplate.opsForHash().entries(key);

        if (cartMap.isEmpty()) {
            return Collections.emptyList();
        }

        List<CartItemVO> items = new ArrayList<>();

        // 遍历购物车中的每个 SKU
        for (Map.Entry<Object, Object> entry : cartMap.entrySet()) {
            Long skuId = Long.parseLong(entry.getKey().toString());
            Integer quantity = Integer.parseInt(entry.getValue().toString());

            // 查询 SKU 信息
            ProductEntity product = productMapper.selectById(skuId);
            if (product == null || product.getIsDel()) {
                // SKU 不存在或已删除，从购物车移除
                redisTemplate.opsForHash().delete(key, skuId.toString());
                continue;
            }

            // 查询商品信息
            GoodsEntity goods = goodsMapper.selectById(product.getGoodsId());
            if (goods == null || (goods.getIsDel() != null && goods.getIsDel() != 0)) {
                // 商品不存在或已删除，从购物车移除
                redisTemplate.opsForHash().delete(key, skuId.toString());
                continue;
            }

            // 构建购物车项 VO
            CartItemVO vo = new CartItemVO();
            vo.setSkuId(skuId);
            vo.setName(goods.getName());
            vo.setImage(product.getImg() != null ? product.getImg() : goods.getImg());
            vo.setSpecText(parseSpecText(product.getSpecArray()));
            vo.setPrice(product.getSellPrice());
            vo.setQuantity(quantity);
            vo.setStock(product.getStoreNums());

            items.add(vo);
        }

        return items;
    }

    /**
     * 更新购物车商品数量
     * 对应原代码：./old/iwebshop/classes/cart.php 第 99-132 行 add() 方法的数量更新部分
     *
     * @param userId   用户ID
     * @param skuId    SKU ID
     * @param quantity 新数量（<=0 时删除该商品）
     */
    public void updateCart(Long userId, Long skuId, Integer quantity) {
        String key = CART_KEY_PREFIX + userId;
        String skuIdStr = skuId.toString();

        if (quantity <= 0) {
            // 数量为 0 或负数，删除该商品
            redisTemplate.opsForHash().delete(key, skuIdStr);
        } else {
            // 检查库存
            ProductEntity product = productMapper.selectById(skuId);
            if (product == null || product.getIsDel()) {
                throw new BusinessException("该商品不存在");
            }
            if (product.getStoreNums() < quantity) {
                throw new BusinessException("该商品库存不足");
            }

            // 更新数量
            redisTemplate.opsForHash().put(key, skuIdStr, String.valueOf(quantity));
        }
    }

    /**
     * 从购物车移除商品
     * 对应原代码：./old/iwebshop/classes/cart.php 第 150-169 行 del() 方法
     *
     * @param userId 用户ID
     * @param skuId  SKU ID
     */
    public void removeFromCart(Long userId, Long skuId) {
        String key = CART_KEY_PREFIX + userId;
        String skuIdStr = skuId.toString();

        // 对应原代码第 159-168 行：检查商品是否存在
        Object existingItem = redisTemplate.opsForHash().get(key, skuIdStr);
        if (existingItem == null) {
            throw new BusinessException("购物车中没有此商品");
        }

        redisTemplate.opsForHash().delete(key, skuIdStr);
    }

    /**
     * 清空购物车
     * 对应原代码：./old/iwebshop/classes/cart.php 第 270-287 行 clear() 方法
     *
     * @param userId 用户ID
     */
    public void clearCart(Long userId) {
        String key = CART_KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }

    /**
     * 合并购物车（登录后合并未登录购物车）
     * 对应原代码：./old/iwebshop/classes/cart.php 第 205-257 行 getMyCartStruct() 方法中的合并逻辑
     *
     * @param userId   用户ID
     * @param deviceId 设备ID（未登录时的临时标识）
     */
    public void mergeCart(Long userId, String deviceId) {
        String userKey = CART_KEY_PREFIX + userId;
        String deviceKey = CART_KEY_PREFIX + "device:" + deviceId;

        // 获取设备购物车数据
        Map<Object, Object> deviceCart = redisTemplate.opsForHash().entries(deviceKey);

        if (deviceCart.isEmpty()) {
            return;
        }

        // 对应原代码第 233-239 行：合并购物车数据
        for (Map.Entry<Object, Object> entry : deviceCart.entrySet()) {
            String skuId = entry.getKey().toString();
            String deviceQty = entry.getValue().toString();

            // 获取用户购物车中该 SKU 的当前数量
            String userQtyStr = (String) redisTemplate.opsForHash().get(userKey, skuId);
            int userQty = userQtyStr != null ? Integer.parseInt(userQtyStr) : 0;

            // 合并数量
            int mergedQty = userQty + Integer.parseInt(deviceQty);

            // 检查库存
            ProductEntity product = productMapper.selectById(Long.parseLong(skuId));
            if (product != null && !product.getIsDel()) {
                if (product.getStoreNums() >= mergedQty) {
                    redisTemplate.opsForHash().put(userKey, skuId, String.valueOf(mergedQty));
                } else {
                    // 库存不足，使用最大库存
                    redisTemplate.opsForHash().put(userKey, skuId, String.valueOf(product.getStoreNums()));
                }
            }
        }

        // 删除设备购物车
        redisTemplate.delete(deviceKey);
    }

    /**
     * 解析规格文本
     * 对应原代码：./old/iwebshop/classes/cart.php 第 323-412 行 cartFormat() 方法中的规格解析
     *
     * @param specArray JSON 格式的规格数组，如：{"颜色":"红色","尺码":"XL"}
     * @return 规格文本，如："红色 XL"
     */
    private String parseSpecText(String specArray) {
        if (specArray == null || specArray.isEmpty()) {
            return "";
        }

        try {
            // 使用 Jackson 解析
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> spec = objectMapper.readValue(
                    specArray,
                    new TypeReference<Map<String, String>>() {}
            );

            // 拼接规格值
            return spec.values().stream()
                    .collect(Collectors.joining(" "));
        } catch (Exception e) {
            return "";
        }
    }
}
