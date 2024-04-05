package com.service.indianfrog.global.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    NOT_FOUND_EMAIL("존재하지 않는 이메일 입니다."),
    ALREADY_EXIST_EMAIL("중복된 이메일입니다."),
    ALREADY_EXIST_NICKNAME("중복된 닉네임입니다."),
    NOT_FOUND_USER("존재하지 않는 회원입니다."),
    ;

    private final String message;

    ErrorCode(String msg) {
        this.message = msg;
    }
}
