package com.phoenix.api.controller;

import com.phoenix.api.dto.request.LoginDTO;
import com.phoenix.api.dto.request.RegisterDTO;
import com.phoenix.api.dto.response.LoginResponseDTO;
import com.phoenix.api.result.Result;
import com.phoenix.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginResponseDTO response = userService.login(loginDTO);
        return Result.success(response);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success();
    }

    @PostMapping("/send-mobile-code")
    public Result<Void> sendMobileCode(@RequestParam String mobile, @RequestParam String captchaKey, @RequestParam String captcha) {
        userService.sendMobileCode(mobile, captchaKey, captcha);
        return Result.success();
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success();
    }

    @GetMapping("/check-mail")
    public Result<Void> checkMail(@RequestParam String code) {
        userService.checkMail(code);
        return Result.success();
    }

    @PostMapping("/send-check-mail")
    public Result<Void> sendCheckMail(@RequestParam String email) {
        userService.sendCheckMail(email);
        return Result.success();
    }

    /**
     * TODO: 第三方 OAuth 登录，先给一个空实现，后续再完善
     */
    @PostMapping("/oauth-login")
    public Result<Void> oauthLogin(@RequestParam Integer id) {
        return Result.success();
    }

    /**
     * TODO: 第三方 OAuth 回调，先给一个空实现，后续再完善
     */
    @GetMapping("/oauth-callback")
    public Result<Void> oauthCallback(@RequestParam String oauthName) {
        return Result.success();
    }

    /**
     * TODO: 绑定已存在用户，先给一个空实现，后续再完善
     */
    @PostMapping("/bind-existing-user")
    public Result<Void> bindExistingUser() {
        return Result.success();
    }

    /**
     * TODO: 绑定注册新用户，先给一个空实现，后续再完善
     */
    @PostMapping("/bind-not-existing-user")
    public Result<Void> bindNotExistingUser() {
        return Result.success();
    }
}