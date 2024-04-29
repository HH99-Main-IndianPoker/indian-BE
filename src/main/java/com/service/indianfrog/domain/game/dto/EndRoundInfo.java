package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.game.entity.Card;
import lombok.Getter;

@Getter
public class EndRoundInfo {

    private String nowState;
    private String nextState;
    private int round;
    private Card myCard;
    private String roundWinner;
    private String roundLoser;
    private int roundPot;
    private int winnerPoint;
    private int loserPoint;

    public EndRoundInfo(String nowState, String nextState, int round, String roundWinner, String roundLoser, int roundPot, Card myCard, int winnerPoint, int loserPoint) {
        this.nowState = nowState;
        this.nextState = nextState;
        this.round = round;
        this.myCard = myCard;
        this.roundWinner = roundWinner;
        this.roundLoser = roundLoser;
        this.roundPot = roundPot;
        this.winnerPoint = winnerPoint;
        this.loserPoint = loserPoint;
    }


}
