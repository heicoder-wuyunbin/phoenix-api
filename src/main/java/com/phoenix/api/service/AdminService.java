package com.phoenix.api.service;

import com.phoenix.api.dto.request.AdminLoginDTO;
import com.phoenix.api.vo.AdminLoginVO;

public interface AdminService {
    AdminLoginVO login(AdminLoginDTO loginDTO);
}