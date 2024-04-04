package com.example.socketpractice.domain.game.entity;

import lombok.Getter;

@Getter
public enum GameState {
    /* 게임 상태 Enum Class
    * READY : 게임 준비
    * START : 게임 시작
    * ACTION : 유저 행동
    * BET : 배팅
    * END : 라운드 종료
    * LEAVE : 게임 방 떠나기
    * GAME_END : 게임 종료
    * USER_CHOICE : 유저 선택(게임 재시작, 게임 나가기)*/
    READY("READY"),
    START("START"),
    ACTION("ACTION"),
    END("END"),
    BET("BET"),
    LEAVE("LEAVE"),
    GAME_END("GAME_END"),
    USER_CHOICE("USER_CHOICE")
    ;

    private final String gameState;
    GameState(String gameState) {
        this.gameState = gameState;
    }
}
