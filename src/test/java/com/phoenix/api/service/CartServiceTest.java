package com.phoenix.api.service;

import com.phoenix.api.entity.GoodsEntity;
import com.phoenix.api.entity.ProductEntity;
import com.phoenix.api.exception.BusinessException;
import com.phoenix.api.mapper.GoodsMapper;
import com.phoenix.api.mapper.ProductMapper;
import com.phoenix.api.vo.CartItemVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService 单元测试")
@SuppressWarnings({"null", "unchecked"})
class CartServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private GoodsMapper goodsMapper;

    @InjectMocks
    private CartService cartService;

    // ==================== addToCart ====================

    @Test
    @DisplayName("addToCart - 正常添加新商品")
    void addToCart_newItem_returnsSuccess() {
        // TC-CART-ADD-UNIT-001
        Long userId = 1L;
        Long skuId = 100L;
        Integer quantity = 2;

        ProductEntity product = new ProductEntity();
        product.setId(skuId);
        product.setGoodsId(200L);
        product.setStoreNums(10);
        product.setIsDel(false);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(productMapper.selectById(skuId)).thenReturn(product);
        when(hashOperations.size(anyString())).thenReturn(0L);
        when(hashOperations.get(anyString(), anyString())).thenReturn(null);

        cartService.addToCart(userId, skuId, quantity);

        verify(hashOperations).put("phoenix:cart:1", "100", "2");
        verify(redisTemplate).expire("phoenix:cart:1", 30L, TimeUnit.DAYS);
    }

    @Test
    @DisplayName("addToCart - 添加已存在的商品，数量累加")
    void addToCart_existingItem_accumulatesQuantity() {
        // TC-CART-ADD-UNIT-001b
        Long userId = 1L;
        Long skuId = 100L;
        Integer quantity = 3;

        ProductEntity product = new ProductEntity();
        product.setId(skuId);
        product.setGoodsId(200L);
        product.setStoreNums(10);
        product.setIsDel(false);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(productMapper.selectById(skuId)).thenReturn(product);
        when(hashOperations.size(anyString())).thenReturn(0L);
        when(hashOperations.get(anyString(), anyString())).thenReturn("2");

        cartService.addToCart(userId, skuId, quantity);

        verify(hashOperations).put("phoenix:cart:1", "100", "5");
    }

    @Test
    @DisplayName("addToCart - SKU 不存在抛出异常")
    void addToCart_skuNotFound_throwsException() {
        // TC-CART-ADD-UNIT-ERR-006
        Long userId = 1L;
        Long skuId = 100L;
        Integer quantity = 1;

        when(productMapper.selectById(skuId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> cartService.addToCart(userId, skuId, quantity));
        assertEquals("该商品不存在", exception.getMessage());
    }

    @Test
    @DisplayName("addToCart - SKU 已删除抛出异常")
    void addToCart_skuDeleted_throwsException() {
        // TC-CART-ADD-UNIT-ERR-006b
        Long userId = 1L;
        Long skuId = 100L;
        Integer quantity = 1;

        ProductEntity product = new ProductEntity();
        product.setId(skuId);
        product.setGoodsId(200L);
        product.setStoreNums(10);
        product.setIsDel(true);

        when(productMapper.selectById(skuId)).thenReturn(product);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> cartService.addToCart(userId, skuId, quantity));
        assertEquals("该商品不存在", exception.getMessage());
    }

    @Test
    @DisplayName("addToCart - 初始库存不足抛出异常")
    void addToCart_insufficientStock_throwsException() {
        // TC-CART-ADD-UNIT-ERR-007
        Long userId = 1L;
        Long skuId = 100L;
        Integer quantity = 5;

        ProductEntity product = new ProductEntity();
        product.setId(skuId);
        product.setGoodsId(200L);
        product.setStoreNums(2);
        product.setIsDel(false);

        when(productMapper.selectById(skuId)).thenReturn(product);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> cartService.addToCart(userId, skuId, quantity));
        assertEquals("该商品库存不足", exception.getMessage());
    }

    @Test
    @DisplayName("addToCart - 累加后库存不足抛出异常")
    void addToCart_accumulatedStockExceeds_throwsException() {
        // TC-CART-ADD-UNIT-ERR-007b
        Long userId = 1L;
        Long skuId = 100L;
        Integer quantity = 3;

        ProductEntity product = new ProductEntity();
        product.setId(skuId);
        product.setGoodsId(200L);
        product.setStoreNums(5);
        product.setIsDel(false);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(productMapper.selectById(skuId)).thenReturn(product);
        when(hashOperations.size(anyString())).thenReturn(0L);
        when(hashOperations.get(anyString(), anyString())).thenReturn("3");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> cartService.addToCart(userId, skuId, quantity));
        assertEquals("该商品库存不足", exception.getMessage());
    }

    @Test
    @DisplayName("addToCart - 购物车容量超限抛出异常")
    void addToCart_cartCapacityExceeded_throwsException() {
        // TC-CART-ADD-UNIT-ERR-010
        Long userId = 1L;
        Long skuId = 100L;
        Integer quantity = 1;

        ProductEntity product = new ProductEntity();
        product.setId(skuId);
        product.setGoodsId(200L);
        product.setStoreNums(10);
        product.setIsDel(false);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(productMapper.selectById(skuId)).thenReturn(product);
        when(hashOperations.size(anyString())).thenReturn(100L);
        when(hashOperations.get(anyString(), anyString())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> cartService.addToCart(userId, skuId, quantity));
        assertEquals("加入购物车失败,购物车中最多只能容纳100种商品", exception.getMessage());
    }

    // ==================== getCartList ====================

    @Test
    @DisplayName("getCartList - 正常获取购物车列表")
    void getCartList_multipleItems_returnsCartItemList() {
        // TC-CART-LIST-UNIT-001
        Long userId = 1L;
        String key = "phoenix:cart:1";
        Long skuId1 = 100L;
        Long skuId2 = 101L;

        Map<Object, Object> cartMap = new HashMap<>();
        cartMap.put("100", "2");
        cartMap.put("101", "3");

        ProductEntity product1 = new ProductEntity();
        product1.setId(skuId1);
        product1.setGoodsId(200L);
        product1.setImg("sku1.jpg");
        product1.setSpecArray("{\"颜色\":\"红色\",\"尺寸\":\"M\"}");
        product1.setSellPrice(new BigDecimal("99.99"));
        product1.setStoreNums(10);
        product1.setIsDel(false);

        ProductEntity product2 = new ProductEntity();
        product2.setId(skuId2);
        product2.setGoodsId(201L);
        product2.setImg(null);
        product2.setSpecArray(null);
        product2.setSellPrice(new BigDecimal("59.99"));
        product2.setStoreNums(20);
        product2.setIsDel(false);

        GoodsEntity goods1 = new GoodsEntity();
        goods1.setId(200L);
        goods1.setName("商品A");
        goods1.setImg("goods1.jpg");
        goods1.setIsDel(0);

        GoodsEntity goods2 = new GoodsEntity();
        goods2.setId(201L);
        goods2.setName("商品B");
        goods2.setImg("goods2.jpg");
        goods2.setIsDel(0);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(key)).thenReturn(cartMap);
        when(productMapper.selectById(skuId1)).thenReturn(product1);
        when(productMapper.selectById(skuId2)).thenReturn(product2);
        when(goodsMapper.selectById(200L)).thenReturn(goods1);
        when(goodsMapper.selectById(201L)).thenReturn(goods2);

        List<CartItemVO> result = cartService.getCartList(userId);

        assertNotNull(result);
        assertEquals(2, result.size());

        CartItemVO vo1 = result.stream().filter(v -> v.getSkuId().equals(100L)).findFirst().orElse(null);
        assertNotNull(vo1);
        assertEquals("商品A", vo1.getName());
        assertEquals("sku1.jpg", vo1.getImage());
        assertEquals("红色 M", vo1.getSpecText());
        assertEquals(new BigDecimal("99.99"), vo1.getPrice());
        assertEquals(2, vo1.getQuantity());
        assertEquals(10, vo1.getStock());

        CartItemVO vo2 = result.stream().filter(v -> v.getSkuId().equals(101L)).findFirst().orElse(null);
        assertNotNull(vo2);
        assertEquals("商品B", vo2.getName());
        assertEquals("goods2.jpg", vo2.getImage());
        assertEquals("", vo2.getSpecText());
        assertEquals(new BigDecimal("59.99"), vo2.getPrice());
        assertEquals(3, vo2.getQuantity());
        assertEquals(20, vo2.getStock());
    }

    @Test
    @DisplayName("getCartList - 购物车为空返回空列表")
    void getCartList_emptyCart_returnsEmptyList() {
        // TC-CART-LIST-UNIT-002
        Long userId = 1L;
        String key = "phoenix:cart:1";

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(key)).thenReturn(Collections.emptyMap());

        List<CartItemVO> result = cartService.getCartList(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getCartList - SKU 已删除自动移除")
    void getCartList_skuDeleted_autoRemovesItem() {
        // TC-CART-LIST-UNIT-ERR-004
        Long userId = 1L;
        String key = "phoenix:cart:1";
        Long skuId = 100L;

        Map<Object, Object> cartMap = new HashMap<>();
        cartMap.put("100", "2");

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(key)).thenReturn(cartMap);
        when(productMapper.selectById(skuId)).thenReturn(null);

        List<CartItemVO> result = cartService.getCartList(userId);

        assertTrue(result.isEmpty());
        verify(hashOperations).delete(key, "100");
    }

    @Test
    @DisplayName("getCartList - 商品已删除自动移除")
    void getCartList_goodsDeleted_autoRemovesItem() {
        // TC-CART-LIST-UNIT-ERR-005
        Long userId = 1L;
        String key = "phoenix:cart:1";
        Long skuId = 100L;

        Map<Object, Object> cartMap = new HashMap<>();
        cartMap.put("100", "2");

        ProductEntity product = new ProductEntity();
        product.setId(skuId);
        product.setGoodsId(200L);
        product.setStoreNums(10);
        product.setIsDel(false);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(key)).thenReturn(cartMap);
        when(productMapper.selectById(skuId)).thenReturn(product);
        when(goodsMapper.selectById(200L)).thenReturn(null);

        List<CartItemVO> result = cartService.getCartList(userId);

        assertTrue(result.isEmpty());
        verify(hashOperations).delete(key, "100");
    }

    // ==================== updateCart ====================

    @Test
    @DisplayName("updateCart - 正常更新数量")
    void updateCart_validQuantity_updatesSuccessfully() {
        Long userId = 1L;
        Long skuId = 100L;
        Integer quantity = 5;

        ProductEntity product = new ProductEntity();
        product.setId(skuId);
        product.setGoodsId(200L);
        product.setStoreNums(10);
        product.setIsDel(false);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(productMapper.selectById(skuId)).thenReturn(product);

        cartService.updateCart(userId, skuId, quantity);

        verify(hashOperations).put("phoenix:cart:1", "100", "5");
    }

    @Test
    @DisplayName("updateCart - 数量为0触发删除")
    void updateCart_quantityZero_deletesItem() {
        Long userId = 1L;
        Long skuId = 100L;
        Integer quantity = 0;

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        cartService.updateCart(userId, skuId, quantity);

        verify(hashOperations).delete("phoenix:cart:1", "100");
    }

    @Test
    @DisplayName("updateCart - 数量为负触发删除")
    void updateCart_quantityNegative_deletesItem() {
        Long userId = 1L;
        Long skuId = 100L;
        Integer quantity = -1;

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        cartService.updateCart(userId, skuId, quantity);

        verify(hashOperations).delete("phoenix:cart:1", "100");
    }

    @Test
    @DisplayName("updateCart - SKU 不存在抛出异常")
    void updateCart_skuNotFound_throwsException() {
        Long userId = 1L;
        Long skuId = 100L;
        Integer quantity = 5;

        when(productMapper.selectById(skuId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> cartService.updateCart(userId, skuId, quantity));
        assertEquals("该商品不存在", exception.getMessage());
    }

    @Test
    @DisplayName("updateCart - 库存不足抛出异常")
    void updateCart_insufficientStock_throwsException() {
        Long userId = 1L;
        Long skuId = 100L;
        Integer quantity = 10;

        ProductEntity product = new ProductEntity();
        product.setId(skuId);
        product.setGoodsId(200L);
        product.setStoreNums(3);
        product.setIsDel(false);

        when(productMapper.selectById(skuId)).thenReturn(product);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> cartService.updateCart(userId, skuId, quantity));
        assertEquals("该商品库存不足", exception.getMessage());
    }

    // ==================== removeFromCart ====================

    @Test
    @DisplayName("removeFromCart - 正常移除商品")
    void removeFromCart_itemExists_removesSuccessfully() {
        // TC-CART-REMOVE-UNIT-001
        Long userId = 1L;
        Long skuId = 100L;

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get(anyString(), anyString())).thenReturn("5");

        cartService.removeFromCart(userId, skuId);

        verify(hashOperations).delete("phoenix:cart:1", "100");
    }

    @Test
    @DisplayName("removeFromCart - 商品不在购物车抛出异常")
    void removeFromCart_itemNotInCart_throwsException() {
        // TC-CART-REMOVE-UNIT-ERR-005
        Long userId = 1L;
        Long skuId = 100L;

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get(anyString(), anyString())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> cartService.removeFromCart(userId, skuId));
        assertEquals("购物车中没有此商品", exception.getMessage());
    }

    // ==================== clearCart ====================

    @Test
    @DisplayName("clearCart - 正常清空购物车")
    void clearCart_cartWithItems_clearsSuccessfully() {
        // TC-CART-CLEAR-UNIT-001
        Long userId = 1L;

        cartService.clearCart(userId);

        verify(redisTemplate).delete("phoenix:cart:1");
    }

    @Test
    @DisplayName("clearCart - 清空空购物车（幂等性）")
    void clearCart_emptyCart_idempotent() {
        // TC-CART-CLEAR-UNIT-002
        Long userId = 1L;

        cartService.clearCart(userId);

        verify(redisTemplate).delete("phoenix:cart:1");
    }

    // ==================== mergeCart ====================

    @Test
    @DisplayName("mergeCart - 正常合并购物车")
    void mergeCart_deviceCartNotEmpty_mergesSuccessfully() {
        Long userId = 1L;
        String deviceId = "device123";
        String userKey = "phoenix:cart:1";
        String deviceKey = "phoenix:cart:device:device123";

        Map<Object, Object> deviceCart = new HashMap<>();
        deviceCart.put("100", "3");
        deviceCart.put("101", "5");

        ProductEntity product1 = new ProductEntity();
        product1.setId(100L);
        product1.setStoreNums(10);
        product1.setIsDel(false);

        ProductEntity product2 = new ProductEntity();
        product2.setId(101L);
        product2.setStoreNums(10);
        product2.setIsDel(false);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(deviceKey)).thenReturn(deviceCart);
        when(hashOperations.get(userKey, "100")).thenReturn(null);
        when(hashOperations.get(userKey, "101")).thenReturn("2");
        when(productMapper.selectById(100L)).thenReturn(product1);
        when(productMapper.selectById(101L)).thenReturn(product2);

        cartService.mergeCart(userId, deviceId);

        verify(hashOperations).put(userKey, "100", "3");
        verify(hashOperations).put(userKey, "101", "7");
        verify(redisTemplate).delete(deviceKey);
    }

    @Test
    @DisplayName("mergeCart - 设备购物车为空不执行操作")
    void mergeCart_deviceCartEmpty_doesNothing() {
        Long userId = 1L;
        String deviceId = "device123";
        String deviceKey = "phoenix:cart:device:device123";

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(deviceKey)).thenReturn(Collections.emptyMap());

        cartService.mergeCart(userId, deviceId);

        verify(hashOperations, never()).put(anyString(), anyString(), anyString());
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("mergeCart - 库存不足时使用最大可用库存")
    void mergeCart_insufficientStock_usesMaxStock() {
        Long userId = 1L;
        String deviceId = "device123";
        String userKey = "phoenix:cart:1";
        String deviceKey = "phoenix:cart:device:device123";

        Map<Object, Object> deviceCart = new HashMap<>();
        deviceCart.put("100", "10");

        ProductEntity product = new ProductEntity();
        product.setId(100L);
        product.setStoreNums(5);
        product.setIsDel(false);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(deviceKey)).thenReturn(deviceCart);
        when(hashOperations.get(userKey, "100")).thenReturn(null);
        when(productMapper.selectById(100L)).thenReturn(product);

        cartService.mergeCart(userId, deviceId);

        verify(hashOperations).put(userKey, "100", "5");
        verify(redisTemplate).delete(deviceKey);
    }
}