package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.game.entity.GameState;
import lombok.Getter;

@Getter
public class GameStatus {
    private Long gameRoomId;
    private String nickname;
    private GameState gameState;

    public GameStatus(Long gameRoomId, String nickname, GameState gameState) {
        this.gameRoomId = gameRoomId;
        this.nickname = nickname;
        this.gameState = gameState;
    }
}
