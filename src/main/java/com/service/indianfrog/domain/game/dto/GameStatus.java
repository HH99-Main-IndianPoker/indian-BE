package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.game.entity.GameState;
import lombok.Getter;

@Getter
public class GameStatus {
    private Long gameRoomId;
    private String userId;
    private GameState gameState;

    public GameStatus(Long gameRoomId, String userId, GameState gameState) {
        this.gameRoomId = gameRoomId;
        this.userId = userId;
        this.gameState = gameState;
    }
}
