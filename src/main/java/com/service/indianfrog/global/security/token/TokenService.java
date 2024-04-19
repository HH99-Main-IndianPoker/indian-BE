package com.service.indianfrog.global.security.token;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenService {

    private final RedisTemplate<String,String> redisTemplate;
//    3900 35분(5분정도시간늘림)
    private static final long REFRESH_TOKEN_EXPIRATION = 60*60*24;

    public TokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public void saveTokenInfo(String email, String refreshToken, String accessToken) {
        String refreshTokenKey = "refreshToken:" + email;
        setTokenWithExpiration(refreshTokenKey,refreshToken,REFRESH_TOKEN_EXPIRATION);
    }

    private void setTokenWithExpiration(String key, String token, long expiration) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set(key,token,expiration, TimeUnit.SECONDS);
    }
}
