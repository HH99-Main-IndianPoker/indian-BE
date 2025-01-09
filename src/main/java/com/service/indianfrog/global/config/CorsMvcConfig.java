package com.service.indianfrog.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**") // 모든 경로 허용
                .exposedHeaders("Authorization", "Set-Cookie") // 노출 헤더 설정
                .allowCredentials(true) // 쿠키 허용
                .allowedOrigins("https://www.indianfrog.com", "http://localhost:3000") // 허용할 도메인 추가
                .allowedMethods("GET", "POST", "PUT", "DELETE"); // 허용할 HTTP 메서드 추가

        // Swagger 경로 허용
        corsRegistry.addMapping("/swagger-ui/**")
                .allowedOrigins("https://www.indianfrog.com", "http://localhost:3000") // 허용할 도메인 추가
                .allowedMethods("GET", "POST");

        corsRegistry.addMapping("/v3/api-docs/**")
                .allowedOrigins("https://www.indianfrog.com", "http://localhost:3000") // 허용할 도메인 추가
                .allowedMethods("GET");
    }
}
