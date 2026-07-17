package com.phoenix.api.service;

import com.phoenix.api.dto.request.LoginDTO;
import com.phoenix.api.dto.request.RegisterDTO;
import com.phoenix.api.dto.response.LoginResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UserService {
    LoginResponseDTO login(LoginDTO loginDTO);

    void register(RegisterDTO registerDTO);

    void sendMobileCode(String mobile, String captchaKey, String captcha);

    void logout();

    void checkMail(String code);

    void sendCheckMail(String email);

    void updatePassword(Long userId, Map<String, Object> params);

    String updateAvatar(Long userId, MultipartFile file);
}