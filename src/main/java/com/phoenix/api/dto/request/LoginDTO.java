package com.phoenix.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank(message = "请填写用户名，邮箱，手机号")
    private String loginInfo;

    @NotBlank(message = "请填写密码")
    @Pattern(regexp = "\\S{6,32}", message = "密码格式不正确,请输入6-32个字符")
    private String password;

    private Integer remember;
}