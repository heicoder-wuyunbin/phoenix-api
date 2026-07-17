package com.phoenix.api.service;

import java.util.List;
import java.util.Map;

public interface AddressService {
    List<Map<String, Object>> getList(Long userId);

    Integer add(Long userId, Map<String, Object> params);

    void update(Long userId, Long id, Map<String, Object> params);

    void delete(Long userId, Long id);

    void setDefault(Long userId, Long id);
}