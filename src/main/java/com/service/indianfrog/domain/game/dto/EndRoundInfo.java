package com.service.indianfrog.domain.game.dto;

import lombok.Getter;

@Getter
public class EndRoundInfo {

    private String nowState;
    private String nextState;
    private int round;
    private String roundWinner;
    private String roundLoser;
    private int roundPot;

    public EndRoundInfo(String nowState, String nextState, int round, String roundWinner, String roundLoser, int roundPot) {
        this.nowState = nowState;
        this.nextState = nextState;
        this.round = round;
        this.roundWinner = roundWinner;
        this.roundLoser = roundLoser;
        this.roundPot = roundPot;
    }


}
