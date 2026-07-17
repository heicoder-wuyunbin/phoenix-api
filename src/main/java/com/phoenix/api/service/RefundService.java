package com.phoenix.api.service;

import java.util.Map;

public interface RefundService {
    Map<String, Object> getList(Long userId, Integer page, Integer pageSize);

    Integer add(Long userId, Map<String, Object> params);

    Map<String, Object> getDetail(Long userId, Long id);

    void delete(Long userId, Long id);
}