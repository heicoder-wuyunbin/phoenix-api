package com.phoenix.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterDTO {
    private String email;

    private String mobile;

    private String mobileCode;

    @NotBlank(message = "请填写用户名")
    @Pattern(regexp = "^[\\w\\u0391-\\uFFE5]{2,20}$", message = "用户名必须是由2-20个字符，可以为字母，数字下划线和中文")
    private String username;

    @NotBlank(message = "请填写密码")
    @Pattern(regexp = "\\S{6,32}", message = "密码是字母，数字，下划线组成的6-32个字符")
    private String password;

    @NotBlank(message = "请确认密码")
    private String repassword;

    @NotBlank(message = "请填写验证码")
    private String captcha;

    private String captchaKey;
}