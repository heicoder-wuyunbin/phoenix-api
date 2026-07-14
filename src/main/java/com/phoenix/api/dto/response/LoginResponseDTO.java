package com.phoenix.api.dto.response;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private Long userId;
    private String username;
    private String headIco;
    private String token;
}