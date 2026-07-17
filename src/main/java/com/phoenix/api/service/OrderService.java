package com.phoenix.api.service;

import java.util.Map;

public interface OrderService {
    Map<String, Object> getList(Long userId, String status, Integer page, Integer pageSize);

    Map<String, Object> getDetail(Long userId, Long id);

    void cancel(Long userId, Long id);

    void confirm(Long userId, Long id);
}