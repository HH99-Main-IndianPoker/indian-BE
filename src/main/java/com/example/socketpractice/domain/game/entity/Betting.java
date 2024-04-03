package com.example.socketpractice.domain.game.entity;

import lombok.Getter;

@Getter
public enum Betting {
    /* 배팅 상태 Enum Class
    * CHECK : 상대와 같은 판돈 걸기
    * RAISE : 판돈의 2배 걸기
    * DIE : 라운드 포기하기 */
    CHECK("CHECK"),
    RAISE("RAISE"),
    DIE("DIE")
    ;

    private final String betting;
    Betting(String betting) {
        this.betting = betting;
    }
}
