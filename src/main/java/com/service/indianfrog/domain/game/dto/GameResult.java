package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.user.entity.User;
import lombok.Getter;

@Getter
public class GameResult {

    private Long winnerId;
    private Long loserId;
    private int winnerPot;
    private int loserPot;

    public GameResult(User winner, User loser) {
        this.winnerId = winner.getId();
        this.loserId = loser.getId();
    }

    public GameResult(User winner, User loser, int winnerPot, int loserPot) {
        this.winnerId = winner.getId();
        this.loserId = loser.getId();
        this.winnerPot = winnerPot;
        this.loserPot = loserPot;
    }
}
