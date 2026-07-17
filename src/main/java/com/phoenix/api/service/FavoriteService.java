package com.phoenix.api.service;

import java.util.Map;

public interface FavoriteService {
    Map<String, Object> getList(Long userId, Integer page, Integer pageSize);

    void delete(Long userId, Long id);

    void updateSummary(Long userId, Long id, String summary);
}