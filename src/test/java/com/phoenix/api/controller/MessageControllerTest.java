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
@DisplayName("MessageController 单元测试")
class MessageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private com.phoenix.api.service.MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(messageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/message/list - 获取消息列表成功")
    void list_validRequest_returnsSuccess() throws Exception {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("total", 6);
        pageResult.put("current", 1);
        pageResult.put("pages", 1);
        pageResult.put("unread", 2);
        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> msg = new HashMap<>();
        msg.put("id", 1);
        msg.put("title", "系统通知");
        msg.put("content", "您的订单已发货");
        msg.put("isRead", false);
        msg.put("createTime", "2026-07-17 10:00:00");
        records.add(msg);
        pageResult.put("records", records);

        when(messageService.getList(eq(1L), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/api/message/list")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(6))
                .andExpect(jsonPath("$.data.unread").value(2))
                .andExpect(jsonPath("$.data.records[0].title").value("系统通知"));
    }

    @Test
    @DisplayName("GET /api/message/list - 无消息时返回空列表")
    void list_noMessages_returnsEmptyList() throws Exception {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("total", 0);
        pageResult.put("current", 1);
        pageResult.put("pages", 0);
        pageResult.put("unread", 0);
        pageResult.put("records", Collections.emptyList());

        when(messageService.getList(eq(1L), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/api/message/list")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.unread").value(0));
    }

    @Test
    @DisplayName("GET /api/message/{id} - 阅读消息成功")
    void read_validRequest_returnsSuccess() throws Exception {
        Map<String, Object> msg = new HashMap<>();
        msg.put("id", 1);
        msg.put("title", "系统通知");
        msg.put("content", "您的订单已发货，物流单号：SF1234567890");
        msg.put("createTime", "2026-07-17 10:00:00");

        when(messageService.read(1L, 1L)).thenReturn(msg);

        mockMvc.perform(get("/api/message/1")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.content").value("您的订单已发货，物流单号：SF1234567890"));
    }

    @Test
    @DisplayName("GET /api/message/{id} - 阅读其他用户消息返回 403")
    void read_otherUserMessage_returnsForbidden() throws Exception {
        when(messageService.read(1L, 999L)).thenThrow(new BusinessException(403, "无权操作"));

        mockMvc.perform(get("/api/message/999")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("GET /api/message/{id} - 消息不存在返回 404")
    void read_messageNotFound_returnsNotFound() throws Exception {
        when(messageService.read(1L, 99999L)).thenThrow(new BusinessException(404, "消息不存在"));

        mockMvc.perform(get("/api/message/99999")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("DELETE /api/message/{id} - 删除消息成功")
    void delete_validRequest_returnsSuccess() throws Exception {
        doNothing().when(messageService).deleteMessage(1L, 2L);

        mockMvc.perform(delete("/api/message/2")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));
    }

    @Test
    @DisplayName("DELETE /api/message/{id} - 删除其他用户消息返回 403")
    void delete_otherUserMessage_returnsForbidden() throws Exception {
        doThrow(new BusinessException(403, "无权操作"))
                .when(messageService).deleteMessage(1L, 999L);

        mockMvc.perform(delete("/api/message/999")
                        .header("Authorization", "Bearer test-token")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("GET /api/message/list - 未登录返回 401")
    void list_noAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/message/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}