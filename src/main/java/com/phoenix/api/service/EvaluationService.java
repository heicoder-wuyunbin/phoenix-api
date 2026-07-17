package com.phoenix.api.service;

import java.util.Map;

public interface EvaluationService {
    Map<String, Object> getList(Long userId, String type, Integer page, Integer pageSize);

    void add(Long userId, Map<String, Object> params);
}