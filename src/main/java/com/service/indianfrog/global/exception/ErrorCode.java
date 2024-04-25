package com.service.indianfrog.global.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    NOT_FOUND_EMAIL("존재하지 않는 이메일 입니다."),
    ALREADY_EXIST_EMAIL("중복된 이메일입니다."),
    ALREADY_EXIST_NICKNAME("중복된 닉네임입니다."),
    NOT_FOUND_GAME_USER("게임방에 회원이 존재하지 않습니다."),
    GAME_USER_HAS_GONE("게임방을 상대방이 나갔습니다."),
    NOT_FOUND_USER("존재하지 않는 회원입니다."),
    ALREADY_EXIST_USER("이미 방에 존재하는 유저입니다."),
    NOT_FOUND_GAME_ROOM("존재하지 않는 게임 방입니다."),
    BATTING_INPUT_EXCEPTION("베팅값의 오류가 발생했습니다."),
    IMPOSSIBLE_UPDATE_REFRESH_TOKEN("블랙리스트된 엑세스토큰입니다."),
    EXPIRED_REFRESH_TOKEN("리프레시 토큰 만료 됐습니다."),
    GAME_ROOM_NOW_FULL("게임방이 가득 찼습니다."),
    NOT_FOUND_HOST("방장을 찾지 못했습니다."),
    INSUFFICIENT_POINTS("포인트가 부족해 게임을 할 수 없습니다."),
    EMAIL_SEND_FAILURE("이메일이 발송되지 않았습니다. 이메일을 다시 확인하세요.")
    ;

    private final String message;

    ErrorCode(String msg) {
        this.message = msg;
    }
}
