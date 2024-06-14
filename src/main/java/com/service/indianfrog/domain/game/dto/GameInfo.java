package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.game.entity.Turn;
import lombok.Getter;

@Getter
public class GameInfo {
    private Card otherCard;
    private String currentPlayer;
    private int firstBet;
    private int roundPot;
    private int round;
    private int myPoint;
    private int otherPoint;

    public GameInfo(Card otherCard, Turn turn, int firstBet, int pot, int round, int myPoint, int otherPoint) {
        this.otherCard = otherCard;
        this.currentPlayer = turn.getCurrentPlayer();
        this.firstBet = firstBet;
        this.roundPot = pot;
        this.round = round;
        this.myPoint = myPoint;
        this.otherPoint = otherPoint;
    }
}
