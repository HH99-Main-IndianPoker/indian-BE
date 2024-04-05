package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.user.entity.User;
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

    @Getter
    public static class EndRoundResponse {
        private String gameState;
        private int round;
        private User roundWinner;
        private User roundLoser;
        private int roundPot;

        public EndRoundResponse(String gameState, int round, User roundWinner, User roundLoser, int roundPot) {
            this.gameState = gameState;
            this.round = round;
            this.roundWinner = roundWinner;
            this.roundLoser = roundLoser;
            this.roundPot = roundPot;
        }
    }

    @Getter
    public static class EndGameResponse {
        private String gameState;
        private User gameWinner;
        private User gameLoser;
        private int winnerPot;
        private int loserPot;
        public EndGameResponse(String gameState, User gameWinner, User gameLoser, int winnerPot, int loserPot) {
            this.gameState = gameState;
            this.gameWinner = gameWinner;
            this.gameLoser = gameLoser;
            this.winnerPot = winnerPot;
            this.loserPot = loserPot;
        }
    }
}
