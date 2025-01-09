package com.service.indianfrog.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Indian Frog API Documentation")
                        .description("API documentation for Indian Frog Project")
                        .version("1.0.0"))
                .addServersItem(new Server().url("https://api.indianfrog.com"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(
                        new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme())
                );
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .name("Authorization");
    }
}