package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.user.entity.User;
import lombok.Getter;

@Getter
public class EndGameInfo {

    private String nowState;
    private String nextState;
    private String gameWinner;
    private String gameLoser;
    private int winnerPot;
    private int loserPot;

    public EndGameInfo(String nowState, String nextState, String gameWinner, String gameLoser, int winnerPot, int loserPot) {
        this.nowState = nowState;
        this.nextState = nextState;
        this.gameWinner = gameWinner;
        this.gameLoser = gameLoser;
        this.winnerPot = winnerPot;
        this.loserPot = loserPot;
    }

}
