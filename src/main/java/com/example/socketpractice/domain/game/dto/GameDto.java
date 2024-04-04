package com.example.socketpractice.domain.game.dto;

import lombok.Getter;

@Getter
public class GameDto {

    @Getter
    public static class StartRoundResponse {
        private String gameState;
        private int round;

        public StartRoundResponse(String gameState, int round) {
            this.gameState = gameState;
            this.round = round;
        }
    }
}
