package com.phoenix.api.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.phoenix.api.entity.AdminEntity;
import com.phoenix.api.mapper.AdminMapper;
import com.phoenix.api.util.MD5Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminMapper adminMapper;

    @Override
    public void run(String... args) {
        AdminEntity existingAdmin = adminMapper.selectOne(
                Wrappers.lambdaQuery(AdminEntity.class)
                        .eq(AdminEntity::getAdminName, "admin")
        );
        if (existingAdmin == null) {
            AdminEntity admin = new AdminEntity();
            admin.setAdminName("admin");
            admin.setPassword(MD5Util.encode("admin"));
            admin.setRoleId(0);
            admin.setCreateTime(LocalDateTime.now());
            admin.setEmail("admin@phoenix.com");
            admin.setIsDel(false);
            adminMapper.insert(admin);
            log.info("默认管理员账号已初始化: admin / admin");
        }
    }
}