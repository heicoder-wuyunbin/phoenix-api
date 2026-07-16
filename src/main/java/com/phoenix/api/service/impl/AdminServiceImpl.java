package com.phoenix.api.service.impl;

import com.phoenix.api.dto.request.AdminLoginDTO;
import com.phoenix.api.entity.AdminEntity;
import com.phoenix.api.exception.BusinessException;
import com.phoenix.api.mapper.AdminMapper;
import com.phoenix.api.service.AdminService;
import com.phoenix.api.util.JwtUtil;
import com.phoenix.api.util.MD5Util;
import com.phoenix.api.vo.AdminLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final JwtUtil jwtUtil;

    @Override
    public AdminLoginVO login(AdminLoginDTO loginDTO) {
        AdminEntity admin = adminMapper.findByAdminName(loginDTO.getUsername());
        if (admin == null) {
            throw new BusinessException("账号或密码错误");
        }

        String encryptedPassword = MD5Util.encode(loginDTO.getPassword());
        if (!encryptedPassword.equals(admin.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }

        // 更新最后登录时间和IP
        admin.setLastTime(LocalDateTime.now());
        admin.setLastIp("127.0.0.1");
        adminMapper.updateById(admin);

        String token = jwtUtil.generateToken(admin.getId(), admin.getAdminName());

        AdminLoginVO vo = new AdminLoginVO();
        vo.setToken(token);

        AdminLoginVO.AdminUserVO userInfo = new AdminLoginVO.AdminUserVO();
        userInfo.setId(admin.getId());
        userInfo.setAdminName(admin.getAdminName());
        userInfo.setRoleId(admin.getRoleId());
        userInfo.setEmail(admin.getEmail());
        if (admin.getLastTime() != null) {
            userInfo.setLastLoginTime(admin.getLastTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        userInfo.setLastLoginIp(admin.getLastIp());
        // TODO: 从 tb_admin_role 表查询角色名称
        userInfo.setRoleName("超级管理员");
        vo.setUserInfo(userInfo);

        return vo;
    }
}