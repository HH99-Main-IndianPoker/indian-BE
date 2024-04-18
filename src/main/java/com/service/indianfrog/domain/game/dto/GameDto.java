package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.game.entity.Turn;
import com.service.indianfrog.domain.user.entity.User;
import lombok.Getter;

@Getter
public class GameDto {

    @Getter
    public static class StartRoundResponse {
        private String gameState;
        private int round;
        private PlayerInfo playerOneInfo;
        private PlayerInfo playerTwoInfo;
        private Turn turn;
        private int firstBet;
        private int roundPot;

        public StartRoundResponse(String gameState, int round, User playerOne, User playerTwo,
                                  Card playerOneCard, Card playerTwoCard, Turn turn, int firstBet) {
            this.gameState = gameState;
            this.round = round;
            this.playerOneInfo = new PlayerInfo(playerOne, playerOneCard);
            this.playerTwoInfo = new PlayerInfo(playerTwo, playerTwoCard);
            this.turn = turn;
            this.firstBet = firstBet;
            this.roundPot = firstBet * 2;

        }
    }

    @Getter
    public static class EndRoundResponse {
        private String nowState;
        private String nextState;
        private int round;
        private String roundWinner;
        private String roundLoser;
        private int roundPot;

        public EndRoundResponse(String nowState, String nextState, int round, String roundWinner, String roundLoser, int roundPot) {
            this.nowState = nowState;
            this.nextState = nextState;
            this.round = round;
            this.roundWinner = roundWinner;
            this.roundLoser = roundLoser;
            this.roundPot = roundPot;
        }
    }

    @Getter
    public static class EndGameResponse {
        private String gameState;
        private String gameWinner;
        private String gameLoser;
        private int winnerPot;
        private int loserPot;

        public EndGameResponse(String gameState, String gameWinner, String gameLoser, int winnerPot, int loserPot) {
            this.gameState = gameState;
            this.gameWinner = gameWinner;
            this.gameLoser = gameLoser;
            this.winnerPot = winnerPot;
            this.loserPot = loserPot;
        }
    }
}
