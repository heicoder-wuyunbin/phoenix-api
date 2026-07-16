package com.phoenix.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenix.api.dto.CartAddDTO;
import com.phoenix.api.exception.BusinessException;
import com.phoenix.api.exception.GlobalExceptionHandler;
import com.phoenix.api.service.CartService;
import com.phoenix.api.vo.CartItemVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartController 单元测试")
@SuppressWarnings("null")
class CartControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ==================== POST /api/cart/add ====================

    @Test
    @DisplayName("POST /api/cart/add - 正常添加新商品到购物车")
    // TC-CART-ADD-UNIT-001
    void add_validRequest_returnsSuccess() throws Exception {
        doNothing().when(cartService).addToCart(anyLong(), anyLong(), anyInt());

        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(1L);
        dto.setQuantity(2);

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("添加成功"));
    }

    @Test
    @DisplayName("POST /api/cart/add - skuId 为空应返回 400")
    // TC-CART-ADD-UNIT-ERR-001
    void add_skuIdNull_returnsBadRequest() throws Exception {
        CartAddDTO dto = new CartAddDTO();
        dto.setQuantity(2);

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("POST /api/cart/add - quantity 为空应返回 400")
    // TC-CART-ADD-UNIT-ERR-002
    void add_quantityNull_returnsBadRequest() throws Exception {
        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(1L);

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("POST /api/cart/add - quantity = 0 应返回 400")
    // TC-CART-ADD-UNIT-ERR-003
    void add_quantityZero_returnsBadRequest() throws Exception {
        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(1L);
        dto.setQuantity(0);

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("POST /api/cart/add - 商品不存在应返回 500")
    // TC-CART-ADD-UNIT-ERR-006
    void add_productNotExist_returnsError() throws Exception {
        doThrow(new BusinessException("该商品不存在"))
                .when(cartService).addToCart(anyLong(), anyLong(), anyInt());

        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(999L);
        dto.setQuantity(1);

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("该商品不存在"));
    }

    @Test
    @DisplayName("POST /api/cart/add - 库存不足应返回 500")
    // TC-CART-ADD-UNIT-ERR-007
    void add_insufficientStock_returnsError() throws Exception {
        doThrow(new BusinessException("该商品库存不足"))
                .when(cartService).addToCart(anyLong(), anyLong(), anyInt());

        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(1L);
        dto.setQuantity(999);

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("该商品库存不足"));
    }

    @Test
    @DisplayName("POST /api/cart/add - 购物车容量超限应返回 500")
    // TC-CART-ADD-UNIT-ERR-010
    void add_cartCapacityExceeded_returnsError() throws Exception {
        doThrow(new BusinessException("加入购物车失败,购物车中最多只能容纳100种商品"))
                .when(cartService).addToCart(anyLong(), anyLong(), anyInt());

        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(1L);
        dto.setQuantity(1);

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("加入购物车失败,购物车中最多只能容纳100种商品"));
    }

    // ==================== GET /api/cart/list ====================

    @Test
    @DisplayName("GET /api/cart/list - 正常获取购物车列表（有多个商品）")
    // TC-CART-LIST-UNIT-001
    void list_cartHasItems_returnsItemList() throws Exception {
        CartItemVO item1 = new CartItemVO();
        item1.setSkuId(1L);
        item1.setName("商品1");
        item1.setImage("image1.jpg");
        item1.setSpecText("红色 XL");
        item1.setPrice(new BigDecimal("99.99"));
        item1.setQuantity(2);
        item1.setStock(10);

        CartItemVO item2 = new CartItemVO();
        item2.setSkuId(2L);
        item2.setName("商品2");
        item2.setImage("image2.jpg");
        item2.setSpecText("蓝色 M");
        item2.setPrice(new BigDecimal("199.99"));
        item2.setQuantity(1);
        item2.setStock(5);

        List<CartItemVO> items = Arrays.asList(item1, item2);
        when(cartService.getCartList(anyLong())).thenReturn(items);

        mockMvc.perform(get("/api/cart/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].skuId").value(1))
                .andExpect(jsonPath("$.data[0].name").value("商品1"))
                .andExpect(jsonPath("$.data[0].quantity").value(2))
                .andExpect(jsonPath("$.data[1].skuId").value(2))
                .andExpect(jsonPath("$.data[1].name").value("商品2"))
                .andExpect(jsonPath("$.data[1].quantity").value(1));
    }

    @Test
    @DisplayName("GET /api/cart/list - 购物车为空时返回空列表")
    // TC-CART-LIST-UNIT-002
    void list_emptyCart_returnsEmptyList() throws Exception {
        when(cartService.getCartList(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/cart/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ==================== PUT /api/cart/update ====================

    @Test
    @DisplayName("PUT /api/cart/update - 正常更新购物车商品数量")
    void update_validRequest_returnsSuccess() throws Exception {
        doNothing().when(cartService).updateCart(anyLong(), anyLong(), anyInt());

        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(1L);
        dto.setQuantity(3);

        mockMvc.perform(put("/api/cart/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("更新成功"));
    }

    @Test
    @DisplayName("PUT /api/cart/update - skuId 为空应返回 400")
    void update_skuIdNull_returnsBadRequest() throws Exception {
        CartAddDTO dto = new CartAddDTO();
        dto.setQuantity(3);

        mockMvc.perform(put("/api/cart/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("PUT /api/cart/update - quantity 为空应返回 400")
    void update_quantityNull_returnsBadRequest() throws Exception {
        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(1L);

        mockMvc.perform(put("/api/cart/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("PUT /api/cart/update - 商品不存在应返回 500")
    void update_productNotExist_returnsError() throws Exception {
        doThrow(new BusinessException("该商品不存在"))
                .when(cartService).updateCart(anyLong(), anyLong(), anyInt());

        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(999L);
        dto.setQuantity(1);

        mockMvc.perform(put("/api/cart/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("该商品不存在"));
    }

    @Test
    @DisplayName("PUT /api/cart/update - 库存不足应返回 500")
    void update_insufficientStock_returnsError() throws Exception {
        doThrow(new BusinessException("该商品库存不足"))
                .when(cartService).updateCart(anyLong(), anyLong(), anyInt());

        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(1L);
        dto.setQuantity(999);

        mockMvc.perform(put("/api/cart/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("该商品库存不足"));
    }

    // ==================== DELETE /api/cart/remove ====================

    @Test
    @DisplayName("DELETE /api/cart/remove - 正常移除商品")
    // TC-CART-REMOVE-UNIT-001
    void remove_validRequest_returnsSuccess() throws Exception {
        doNothing().when(cartService).removeFromCart(anyLong(), anyLong());

        mockMvc.perform(delete("/api/cart/remove")
                        .param("skuId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("移除成功"));
    }

    @Test
    @DisplayName("DELETE /api/cart/remove - skuId 缺失应返回 400")
    // TC-CART-REMOVE-UNIT-ERR-001
    void remove_skuIdMissing_returnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/cart/remove"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("DELETE /api/cart/remove - 购物车中不存在该商品应返回 500")
    // TC-CART-REMOVE-UNIT-ERR-005
    void remove_productNotInCart_returnsError() throws Exception {
        doThrow(new BusinessException("购物车中没有此商品"))
                .when(cartService).removeFromCart(anyLong(), anyLong());

        mockMvc.perform(delete("/api/cart/remove")
                        .param("skuId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("购物车中没有此商品"));
    }

    // ==================== DELETE /api/cart/clear ====================

    @Test
    @DisplayName("DELETE /api/cart/clear - 正常清空购物车")
    // TC-CART-CLEAR-UNIT-001
    void clear_validRequest_returnsSuccess() throws Exception {
        doNothing().when(cartService).clearCart(anyLong());

        mockMvc.perform(delete("/api/cart/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("清空成功"));
    }
}