package com.phoenix.api.service;

import java.util.Map;

public interface WithdrawService {
    void add(Long userId, Map<String, Object> params);

    Map<String, Object> getList(Long userId, Integer page, Integer pageSize);

    void delete(Long userId, Long id);
}