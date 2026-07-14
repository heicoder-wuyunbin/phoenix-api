package com.phoenix.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "site")
public class SiteConfig {

    private Integer regOption = 3;

    private String mobile;

    private String baseUrl = "http://localhost:8080";
}