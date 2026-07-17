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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressController 单元测试")
class AddressControllerTest {

    private MockMvc mockMvc;

    @Mock
    private com.phoenix.api.service.AddressService addressService;

    @InjectMocks
    private AddressController addressController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(addressController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/address/list - 获取地址列表成功")
    void list_validRequest_returnsSuccess() throws Exception {
        Map<String, Object> addr1 = new HashMap<>();
        addr1.put("id", 1);
        addr1.put("acceptName", "张三");
        addr1.put("telphone", "13800138000");
        addr1.put("address", "某街道100号");
        addr1.put("isDefault", true);

        Map<String, Object> addr2 = new HashMap<>();
        addr2.put("id", 2);
        addr2.put("acceptName", "李四");
        addr2.put("telphone", "13900139000");
        addr2.put("address", "某路200号");
        addr2.put("isDefault", false);

        when(addressService.getList(1L)).thenReturn(Arrays.asList(addr1, addr2));

        mockMvc.perform(get("/api/address/list")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].acceptName").value("张三"))
                .andExpect(jsonPath("$.data[1].acceptName").value("李四"));
    }

    @Test
    @DisplayName("GET /api/address/list - 无地址时返回空列表")
    void list_noAddresses_returnsEmptyList() throws Exception {
        when(addressService.getList(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/address/list")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/address/list - 未登录返回 401")
    void list_noAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/address/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("POST /api/address - 新增地址成功")
    void add_validRequest_returnsSuccess() throws Exception {
        when(addressService.add(eq(1L), anyMap())).thenReturn(1);

        String requestBody = "{\"acceptName\":\"王五\",\"province\":110000,\"city\":110100,\"area\":110101,\"address\":\"某街300号\",\"telphone\":\"13700137000\",\"isDefault\":0}";

        mockMvc.perform(post("/api/address")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("POST /api/address - 缺少收货人姓名返回错误")
    void add_missingAcceptName_returnsError() throws Exception {
        String requestBody = "{\"telphone\":\"13700137000\",\"address\":\"某地址\"}";

        mockMvc.perform(post("/api/address")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("PUT /api/address/{id} - 修改地址成功")
    void update_validRequest_returnsSuccess() throws Exception {
        String requestBody = "{\"acceptName\":\"张三(修改后)\",\"telphone\":\"13800138001\",\"address\":\"新地址\"}";

        doNothing().when(addressService).update(eq(1L), eq(1L), anyMap());

        mockMvc.perform(put("/api/address/1")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("PUT /api/address/{id} - 修改其他用户地址返回 403")
    void update_otherUserAddress_returnsForbidden() throws Exception {
        doThrow(new BusinessException("无权操作")).when(addressService).update(eq(1L), eq(999L), anyMap());

        String requestBody = "{\"acceptName\":\"张三\"}";

        mockMvc.perform(put("/api/address/999")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("DELETE /api/address/{id} - 删除地址成功")
    void delete_validRequest_returnsSuccess() throws Exception {
        doNothing().when(addressService).delete(1L, 1L);

        mockMvc.perform(delete("/api/address/1")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("DELETE /api/address/{id} - 删除不存在的地址返回 404")
    void delete_notFound_returnsNotFound() throws Exception {
        doThrow(new BusinessException("地址不存在")).when(addressService).delete(1L, 99999L);

        mockMvc.perform(delete("/api/address/99999")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("PUT /api/address/{id}/default - 设置默认地址成功")
    void setDefault_validRequest_returnsSuccess() throws Exception {
        doNothing().when(addressService).setDefault(1L, 2L);

        mockMvc.perform(put("/api/address/2/default")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}