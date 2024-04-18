package com.service.indianfrog.global.security.token;

import jakarta.persistence.Id;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

@Getter
@RedisHash(value = "jwtToken", timeToLive = 60 * 60 * 24)
public class RefreshToken implements Serializable {

    @Id
    private String id;

    @Indexed
    private String accessToken;

    private String refreshToken;


    public RefreshToken(String id, String accessToken, String refreshToken) {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public void updateAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void updateRefreshToken(String refreshToken1) {
        this.refreshToken = refreshToken1;
    }
}
