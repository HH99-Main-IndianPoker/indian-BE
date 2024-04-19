package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.user.entity.User;
import lombok.Getter;

@Getter
public class GameResult {

    private User winner;
    private User loser;
    private int winnerPot;
    private int loserPot;

    public GameResult(User winner, User loser) {
        this.winner = winner;
        this.loser = loser;
    }

    public GameResult(User winner, User loser, int winnerPot, int loserPot) {
        this.winner = winner;
        this.loser = loser;
        this.winnerPot = winnerPot;
        this.loserPot = loserPot;
    }
}
