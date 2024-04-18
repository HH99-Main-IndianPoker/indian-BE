package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.user.entity.User;
import lombok.Getter;

@Getter
public class GameResult {

    private String winner;
    private String loser;
    private int winnerPot;
    private int loserPot;

    public GameResult(User winner, User loser) {
        this.winner = winner.getNickname();
        this.loser = loser.getNickname();
    }

    public GameResult(User winner, User loser, int winnerPot, int loserPot) {
        this.winner = winner.getNickname();
        this.loser = loser.getNickname();
        this.winnerPot = winnerPot;
        this.loserPot = loserPot;
    }
}
