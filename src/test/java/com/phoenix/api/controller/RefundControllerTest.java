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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefundController 单元测试")
class RefundControllerTest {

    private MockMvc mockMvc;

    @Mock
    private com.phoenix.api.service.RefundService refundService;

    @InjectMocks
    private RefundController refundController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(refundController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/refund/list - 获取退款列表成功")
    void list_validRequest_returnsSuccess() throws Exception {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("total", 3);
        pageResult.put("current", 1);
        pageResult.put("pages", 1);
        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> refund = new HashMap<>();
        refund.put("id", 1);
        refund.put("orderNo", "20260717001");
        refund.put("status", 0);
        refund.put("amount", 99.00);
        refund.put("time", "2026-07-17 10:00:00");
        records.add(refund);
        pageResult.put("records", records);

        when(refundService.getList(eq(1L), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/api/refund/list")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.records[0].orderNo").value("20260717001"));
    }

    @Test
    @DisplayName("POST /api/refund - 提交退款申请成功")
    void add_validRequest_returnsSuccess() throws Exception {
        when(refundService.add(eq(1L), anyMap())).thenReturn(1);

        String requestBody = "{\"orderId\":1,\"goodsIds\":[1,2],\"reason\":\"商品与描述不符\",\"amount\":99.00,\"content\":\"收到的商品颜色不对\"}";

        mockMvc.perform(post("/api/refund")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("POST /api/refund - 退款金额超过订单金额返回错误")
    void add_amountExceeds_returnsError() throws Exception {
        doThrow(new BusinessException(400, "退款金额不能超过订单金额"))
                .when(refundService).add(eq(1L), anyMap());

        String requestBody = "{\"orderId\":2,\"goodsIds\":[1],\"reason\":\"质量问题\",\"amount\":200.00}";

        mockMvc.perform(post("/api/refund")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("退款金额不能超过订单金额"));
    }

    @Test
    @DisplayName("POST /api/refund - 未付款订单提交退款返回错误")
    void add_unpaidOrder_returnsError() throws Exception {
        doThrow(new BusinessException(400, "当前订单状态不允许退款"))
                .when(refundService).add(eq(1L), anyMap());

        String requestBody = "{\"orderId\":3,\"reason\":\"不想要了\",\"amount\":0.00}";

        mockMvc.perform(post("/api/refund")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("当前订单状态不允许退款"));
    }

    @Test
    @DisplayName("GET /api/refund/{id} - 获取退款详情成功")
    void detail_validRequest_returnsSuccess() throws Exception {
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", 1);
        detail.put("orderNo", "20260717001");
        detail.put("status", 0);
        detail.put("reason", "商品与描述不符");
        detail.put("amount", 99.00);
        detail.put("time", "2026-07-17 10:00:00");

        when(refundService.getDetail(1L, 1L)).thenReturn(detail);

        mockMvc.perform(get("/api/refund/1")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.orderNo").value("20260717001"))
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    @DisplayName("GET /api/refund/{id} - 查看其他用户退款详情返回 403")
    void detail_otherUserRefund_returnsForbidden() throws Exception {
        when(refundService.getDetail(1L, 999L)).thenThrow(new BusinessException(403, "无权查看"));

        mockMvc.perform(get("/api/refund/999")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("DELETE /api/refund/{id} - 取消退款申请成功")
    void delete_validRequest_returnsSuccess() throws Exception {
        doNothing().when(refundService).delete(1L, 2L);

        mockMvc.perform(delete("/api/refund/2")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("取消成功"));
    }

    @Test
    @DisplayName("DELETE /api/refund/{id} - 取消已审核通过的退款返回错误")
    void delete_approvedRefund_returnsError() throws Exception {
        doThrow(new BusinessException(400, "当前退款状态不允许取消"))
                .when(refundService).delete(1L, 3L);

        mockMvc.perform(delete("/api/refund/3")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("当前退款状态不允许取消"));
    }

    @Test
    @DisplayName("GET /api/refund/list - 未登录返回 401")
    void list_noAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/refund/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}