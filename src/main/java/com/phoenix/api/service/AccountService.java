package com.phoenix.api.service;

import java.util.Map;

public interface AccountService {
    Map<String, Object> getLog(Long userId, Integer page, Integer pageSize);

    String recharge(Long userId, Map<String, Object> params);
}