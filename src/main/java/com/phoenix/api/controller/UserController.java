package com.phoenix.api.controller;

import com.phoenix.api.dto.request.LoginDTO;
import com.phoenix.api.dto.request.RegisterDTO;
import com.phoenix.api.dto.response.LoginResponseDTO;
import com.phoenix.api.result.Result;
import com.phoenix.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户登录、注册、验证码等接口")
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户登录")
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

    @GetMapping("/check-mail-page")
    public ResponseEntity<String> checkMailPage(@RequestParam String code) {
        try {
            userService.checkMail(code);
            return ResponseEntity.ok(buildSuccessHtml());
        } catch (Exception e) {
            return ResponseEntity.ok(buildErrorHtml(e.getMessage()));
        }
    }

    private String buildSuccessHtml() {
        return "<!DOCTYPE html>" +
                "<html lang=\"zh-CN\">" +
                "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>邮箱验证</title>" +
                "<style>" +
                "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background: #f0f2f5; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }" +
                ".container { background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); padding: 48px; text-align: center; max-width: 400px; width: 100%; }" +
                ".icon-success { width: 72px; height: 72px; background: #52c41a; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 24px; font-size: 36px; color: #fff; }" +
                "h2 { color: #262626; font-size: 24px; margin-bottom: 16px; font-weight: 500; }" +
                "p { color: #595959; font-size: 14px; line-height: 1.8; }" +
                ".hint { color: #8c8c8c; font-size: 12px; margin-top: 24px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"icon-success\">&#10003;</div>" +
                "<h2>邮箱验证成功</h2>" +
                "<p>您的邮箱已成功激活，现在可以使用账号登录系统了。</p>" +
                "<p class=\"hint\">此页面将在 5 秒后自动关闭</p>" +
                "</div>" +
                "<script>setTimeout(() => { window.close(); }, 5000);</script>" +
                "</body>" +
                "</html>";
    }

    private String buildErrorHtml(String message) {
        return "<!DOCTYPE html>" +
                "<html lang=\"zh-CN\">" +
                "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>邮箱验证</title>" +
                "<style>" +
                "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background: #f0f2f5; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }" +
                ".container { background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); padding: 48px; text-align: center; max-width: 400px; width: 100%; }" +
                ".icon-error { width: 72px; height: 72px; background: #ff4d4f; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 24px; font-size: 36px; color: #fff; }" +
                "h2 { color: #262626; font-size: 24px; margin-bottom: 16px; font-weight: 500; }" +
                ".error-message { color: #ff4d4f; font-size: 14px; margin-bottom: 16px; }" +
                ".reasons { text-align: left; color: #595959; font-size: 13px; line-height: 1.8; margin: 24px 0; padding: 16px; background: #fafafa; border-radius: 4px; }" +
                ".reasons ul { margin: 8px 0; padding-left: 20px; }" +
                ".hint { color: #8c8c8c; font-size: 12px; margin-top: 24px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"icon-error\">&#10007;</div>" +
                "<h2>邮箱验证失败</h2>" +
                "<p class=\"error-message\">" + (message != null ? message : "验证信息有误，请核实！") + "</p>" +
                "<div class=\"reasons\">" +
                "<p>可能原因：</p>" +
                "<ul>" +
                "<li>验证链接已过期</li>" +
                "<li>验证链接已被使用</li>" +
                "<li>验证链接格式不正确</li>" +
                "</ul>" +
                "</div>" +
                "<p class=\"hint\">如有疑问，请联系客服</p>" +
                "</div>" +
                "</body>" +
                "</html>";
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