package com.service.indianfrog.domain.game.entity;

import com.service.indianfrog.domain.user.entity.User;
import lombok.Getter;

import java.util.List;

@Getter
public class Turn {
    private List<String> players;
    private int currentPlayer;

    public Turn(List<User> players) {
        this.players = players.stream().map(User::getNickname).toList();
        this.currentPlayer = 0;
    }

    public void nextTurn() {
        this.currentPlayer = (currentPlayer + 1) % players.size();
    }

    public String getCurrentPlayer() {
        return players.get(currentPlayer);
    }

}
