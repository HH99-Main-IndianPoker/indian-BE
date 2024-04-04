package com.example.socketpractice.domain.game.dto;

import com.example.socketpractice.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
