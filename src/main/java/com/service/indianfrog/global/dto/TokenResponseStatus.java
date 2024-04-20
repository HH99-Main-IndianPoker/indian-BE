package com.service.indianfrog.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TokenResponseStatus {

    private Integer status;

    public static TokenResponseStatus addStatus(Integer status) {
        return new TokenResponseStatus(status);
    }
}