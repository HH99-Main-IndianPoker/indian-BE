package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.game.entity.Turn;
import lombok.Getter;

@Getter
public class GameInfo {
    private Card playerCard;
    private String currentPlayer;
    private int firstBet;
    private int roundPot;

    public GameInfo(Card playerCard, Turn turn, int firstBet, int pot) {
        this.playerCard = playerCard;
        this.currentPlayer = turn.getCurrentPlayer();
        this.firstBet = firstBet;
        this.roundPot = pot;
    }
}
