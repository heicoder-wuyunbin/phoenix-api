package com.phoenix.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Phoenix API")
                        .description("Phoenix API Documentation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Phoenix Team")
                                .email("support@phoenix.com")));
    }
}