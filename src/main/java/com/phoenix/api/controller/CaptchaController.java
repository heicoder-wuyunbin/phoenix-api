package com.phoenix.api.controller;

import com.phoenix.api.util.CaptchaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaUtil captchaUtil;

    @GetMapping(value = "/user/captcha", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getCaptcha(@RequestParam(required = false) String key) throws IOException {
        if (key == null || key.isEmpty()) {
            key = UUID.randomUUID().toString();
        }
        return captchaUtil.generateCaptcha(key);
    }
}