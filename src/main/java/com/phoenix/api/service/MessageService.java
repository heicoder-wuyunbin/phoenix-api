package com.phoenix.api.service;

import java.util.Map;

public interface MessageService {
    Map<String, Object> getList(Long userId, Integer page, Integer pageSize);

    Map<String, Object> read(Long userId, Long id);

    void deleteMessage(Long userId, Long id);
}