package com.service.indianfrog.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TokenResponseStatus {

    private Integer status;
    private String token;

    public static TokenResponseStatus addStatus(Integer status, String token) {
        return new TokenResponseStatus(status, token);
    }
}