package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.user.entity.User;
import lombok.Getter;

@Getter
public class GameDto {

    @Getter
    public static class StartRoundResponse {
        private String gameState;
        private int round;
        private Card playerCard;

        public StartRoundResponse(String gameState, int round, Card playerCard) {
            this.gameState = gameState;
            this.round = round;
            this.playerCard = playerCard;
        }
    }

    @Getter
    public static class EndRoundResponse {
        private String gameState;
        private int round;
        private Long roundWinnerId;
        private Long roundLoserId;
        private int roundPot;

        public EndRoundResponse(String gameState, int round, Long roundWinnerId, Long roundLoserId, int roundPot) {
            this.gameState = gameState;
            this.round = round;
            this.roundWinnerId = roundWinnerId;
            this.roundLoserId = roundLoserId;
            this.roundPot = roundPot;
        }
    }

    @Getter
    public static class EndGameResponse {
        private String gameState;
        private Long gameWinnerId;
        private Long gameLoserId;
        private int winnerPot;
        private int loserPot;
        public EndGameResponse(String gameState, Long gameWinner, Long gameLoser, int winnerPot, int loserPot) {
            this.gameState = gameState;
            this.gameWinnerId = gameWinner;
            this.gameLoserId = gameLoser;
            this.winnerPot = winnerPot;
            this.loserPot = loserPot;
        }
    }
}
