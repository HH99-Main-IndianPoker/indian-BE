package com.service.indianfrog.domain.game.dto;

import lombok.Getter;

@Getter
public class GameRequest {
    private Long gameRoomId;
    private String gameState;
    private String email;
    private GameBetting gameBetting;
    private UserChoices userChoices;

    public GameRequest(Long gameRoomId, String gameState, String email, GameBetting gameBetting, UserChoices userChoices) {
        this.gameRoomId = gameRoomId;
        this.gameState = gameState;
        this.email = email;
        this.gameBetting = gameBetting;
        this.userChoices = userChoices;
    }
}
