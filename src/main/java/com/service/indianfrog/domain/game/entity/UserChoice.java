package com.service.indianfrog.domain.game.entity;

public enum UserChoice {

    READY("READY"),
    PLAY_AGAIN("PLAY_AGAIN"), // 게임을 다시 하기로 선택
    LEAVE("LEAVE") // 게임방에서 나가기로 선택
    ;

    private final String userChoice;
    UserChoice(String userChoice) {
        this.userChoice = userChoice;
    }
}
