package com.phoenix.api.service;

import java.util.Map;

public interface MemberService {
    Map<String, Object> getCenter(Long userId);

    Map<String, Object> getProfile(Long userId);

    void updateProfile(Long userId, Map<String, Object> params);
}