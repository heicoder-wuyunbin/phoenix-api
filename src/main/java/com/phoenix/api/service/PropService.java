package com.phoenix.api.service;

import java.util.Map;

public interface PropService {
    Map<String, Object> getList(Long userId, String status, Integer page, Integer pageSize);
}