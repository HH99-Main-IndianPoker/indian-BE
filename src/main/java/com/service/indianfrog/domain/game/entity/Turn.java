package com.service.indianfrog.domain.game.entity;

import com.service.indianfrog.domain.user.entity.User;

import java.util.List;

public class Turn {
    private List<User> players;
    private int currentPlayer;

    public Turn(List<User> players) {
        this.players = players;
        this.currentPlayer = 0;
    }

    public void NextTurn() {
        currentPlayer = (currentPlayer + 1) % players.size();
    }

    public User getCurrentPlayer() {
        return players.get(currentPlayer);
    }
}
