package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.game.entity.Betting;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.game.entity.Turn;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ActionDto {

    String nowState;
    String nextState;
    String actionType;
    String currentPlayer;
    int nowBet;
    int pot;

    @Builder
    public ActionDto(GameState nowState, GameState nextState, Betting actionType, int nowBet, int pot, String currentPlayer){
        this.nowState = nowState.getGameState();
        this.nextState = nextState.getGameState();
        this.actionType = actionType.getBetting();
        this.nowBet = nowBet;
        this.pot = pot;
        this.currentPlayer = currentPlayer;
    }


}
