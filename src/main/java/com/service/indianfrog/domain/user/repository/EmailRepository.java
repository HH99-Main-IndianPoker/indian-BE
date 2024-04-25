package com.service.indianfrog.domain.user.repository;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EmailRepository {

    private final StringRedisTemplate redisTemplate;

    public EmailRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /*이메일 인증 유효기간 5분*/
    public void saveCertificationNumber(String email, String certificationNumber) {
        redisTemplate.opsForValue()
            .set(email, certificationNumber, Duration.ofSeconds(300));
    }

    public String getCertificationNumber(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    public void removeCertificationNumber(String email) {
        redisTemplate.delete(email);
    }

    public boolean hashKey(String email) {
        Boolean keyExists = redisTemplate.hasKey(email);
        return keyExists != null & Boolean.TRUE.equals(keyExists);
    }
}
