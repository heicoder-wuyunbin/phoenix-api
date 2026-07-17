package com.phoenix.api.service;

import java.util.Map;

public interface PointService {
    Map<String, Object> getLog(Long userId, Integer page, Integer pageSize);

    void exchange(Long userId, Integer propId);
}