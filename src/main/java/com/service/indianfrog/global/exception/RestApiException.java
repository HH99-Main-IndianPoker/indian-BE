package com.service.indianfrog.global.exception;

import lombok.Getter;

@Getter
public class RestApiException extends RuntimeException {

    public RestApiException(String msg) {
        super(msg);
    }
}