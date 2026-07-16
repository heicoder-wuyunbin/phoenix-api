package com.phoenix.api.vo;

import lombok.Data;

@Data
public class AdminLoginVO {
    private String token;
    private AdminUserVO userInfo;

    @Data
    public static class AdminUserVO {
        private Long id;
        private String adminName;
        private Integer roleId;
        private String roleName;
        private String email;
        private String lastLoginTime;
        private String lastLoginIp;
    }
}