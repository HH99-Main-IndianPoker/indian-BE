package com.example.socketpractice.domain.game.entity;

import lombok.Getter;

@Getter
public enum GameState {
    /* 게임 상태 Enum Class
    * READY : 게임 준비
    * START : 게임 시작
    * ACTION : 유저 행동
    * BET : 배팅
    * END : 게임 종료 */
    READY("READY"),
    START("START"),
    ACTION("ACTION"),
    END("END"),
    BET("BET")
    ;

    private final String gameState;
    GameState(String gameState) {
        this.gameState = gameState;
    }
}
