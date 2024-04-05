package com.service.indianfrog.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class JwtProperties {
    @Value("${jwt.secret.key.access}")
    private String access;
    @Value("${jwt.secret.key.refresh}")
    private String refresh;
}