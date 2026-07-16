package com.phoenix.api.controller;

import com.phoenix.api.dto.request.AdminLoginDTO;
import com.phoenix.api.result.Result;
import com.phoenix.api.service.AdminService;
import com.phoenix.api.vo.AdminLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "管理员认证", description = "管理员登录、信息获取等接口")
public class AuthController {

    private final AdminService adminService;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<AdminLoginVO> login(@Valid @RequestBody AdminLoginDTO loginDTO) {
        AdminLoginVO response = adminService.login(loginDTO);
        return Result.success(response);
    }

    @Operation(summary = "获取当前管理员信息")
    @GetMapping("/info")
    public Result<AdminLoginVO.AdminUserVO> info() {
        // TODO: 从 token 中获取管理员信息并返回
        AdminLoginVO.AdminUserVO userInfo = new AdminLoginVO.AdminUserVO();
        return Result.success(userInfo);
    }
}