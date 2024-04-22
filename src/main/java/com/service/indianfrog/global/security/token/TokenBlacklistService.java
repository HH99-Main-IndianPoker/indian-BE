package com.service.indianfrog.global.security.token;

import java.time.Duration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blackListToken(String token) {
        redisTemplate.opsForValue().set("blacklist:"+token,"true", Duration.ofHours(24));
    }

    public boolean isTokenBlacklisted(String token) {
        String isBlacklisted = redisTemplate.opsForValue().get("blacklist:" + token);
        return "true".equals(isBlacklisted);
    }
}
