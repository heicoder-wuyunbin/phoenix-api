package com.phoenix.api.controller;

import com.phoenix.api.exception.BusinessException;
import com.phoenix.api.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController 单元测试")
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private com.phoenix.api.service.OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/order/list - 获取订单列表成功")
    void list_validRequest_returnsSuccess() throws Exception {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("total", 15);
        pageResult.put("current", 1);
        pageResult.put("pages", 2);
        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> order1 = new HashMap<>();
        order1.put("id", 1);
        order1.put("orderNo", "20260717001");
        order1.put("status", "wait_pay");
        order1.put("amount", 99.99);
        records.add(order1);
        pageResult.put("records", records);

        when(orderService.getList(eq(1L), any(), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/api/order/list")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(15))
                .andExpect(jsonPath("$.data.records[0].orderNo").value("20260717001"));
    }

    @Test
    @DisplayName("GET /api/order/list - 按状态筛选订单成功")
    void list_filterByStatus_returnsFilteredOrders() throws Exception {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("total", 1);
        pageResult.put("current", 1);
        pageResult.put("pages", 1);
        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> order1 = new HashMap<>();
        order1.put("id", 1);
        order1.put("status", "wait_pay");
        records.add(order1);
        pageResult.put("records", records);

        when(orderService.getList(eq(1L), eq("wait_pay"), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/api/order/list")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .param("status", "wait_pay")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].status").value("wait_pay"));
    }

    @Test
    @DisplayName("GET /api/order/list - 无订单时返回空列表")
    void list_noOrders_returnsEmptyList() throws Exception {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("total", 0);
        pageResult.put("current", 1);
        pageResult.put("pages", 0);
        pageResult.put("records", Collections.emptyList());

        when(orderService.getList(eq(1L), isNull(), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/api/order/list")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("GET /api/order/list - 未登录返回 401")
    void list_noAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/order/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("GET /api/order/{id} - 获取订单详情成功")
    void detail_validRequest_returnsSuccess() throws Exception {
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", 1);
        detail.put("orderNo", "20260717001");
        detail.put("status", "paid");
        detail.put("amount", 99.99);
        detail.put("acceptName", "张三");
        detail.put("acceptMobile", "13800138000");
        detail.put("acceptAddress", "北京市某街道100号");
        List<Map<String, Object>> goodsList = new ArrayList<>();
        Map<String, Object> goods = new HashMap<>();
        goods.put("goodsName", "测试商品");
        goods.put("goodsPrice", 99.99);
        goods.put("goodsNum", 1);
        goodsList.add(goods);
        detail.put("goodsList", goodsList);

        when(orderService.getDetail(1L, 1L)).thenReturn(detail);

        mockMvc.perform(get("/api/order/1")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").value("20260717001"))
                .andExpect(jsonPath("$.data.goodsList[0].goodsName").value("测试商品"));
    }

    @Test
    @DisplayName("GET /api/order/{id} - 查看其他用户订单返回 403")
    void detail_otherUserOrder_returnsForbidden() throws Exception {
        when(orderService.getDetail(1L, 999L)).thenThrow(new BusinessException("无权查看"));

        mockMvc.perform(get("/api/order/999")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("GET /api/order/{id} - 订单不存在返回 404")
    void detail_orderNotFound_returnsNotFound() throws Exception {
        when(orderService.getDetail(1L, 99999L)).thenThrow(new BusinessException("订单不存在"));

        mockMvc.perform(get("/api/order/99999")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("PUT /api/order/{id}/cancel - 取消订单成功")
    void cancel_validRequest_returnsSuccess() throws Exception {
        doNothing().when(orderService).cancel(1L, 1L);

        mockMvc.perform(put("/api/order/1/cancel")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    @DisplayName("PUT /api/order/{id}/cancel - 已发货订单不能取消")
    void cancel_shippedOrder_returnsError() throws Exception {
        doThrow(new BusinessException("当前订单状态不允许取消")).when(orderService).cancel(1L, 2L);

        mockMvc.perform(put("/api/order/2/cancel")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("当前订单状态不允许取消"));
    }

    @Test
    @DisplayName("PUT /api/order/{id}/cancel - 取消其他用户订单返回 403")
    void cancel_otherUserOrder_returnsForbidden() throws Exception {
        doThrow(new BusinessException("无权操作")).when(orderService).cancel(1L, 999L);

        mockMvc.perform(put("/api/order/999/cancel")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("PUT /api/order/{id}/confirm - 确认收货成功")
    void confirm_validRequest_returnsSuccess() throws Exception {
        doNothing().when(orderService).confirm(1L, 4L);

        mockMvc.perform(put("/api/order/4/confirm")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("PUT /api/order/{id}/confirm - 未发货订单不能确认收货")
    void confirm_unshippedOrder_returnsError() throws Exception {
        doThrow(new BusinessException("当前订单状态不允许确认收货")).when(orderService).confirm(1L, 5L);

        mockMvc.perform(put("/api/order/5/confirm")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("当前订单状态不允许确认收货"));
    }
}