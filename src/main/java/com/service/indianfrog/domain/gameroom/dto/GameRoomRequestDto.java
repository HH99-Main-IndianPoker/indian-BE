package com.service.indianfrog.domain.gameroom.dto;

import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;

import java.time.LocalDateTime;

public class GameRoomRequestDto{

    public record GameRoomCreateRequestDto(
            Long roomId,
            String roomName
    ){
        public GameRoom toEntity() {
            return GameRoom.builder()
                    .roomId(roomId)
                    .roomName(roomName)
                    .gameState(GameState.READY)
                    .build();
        }
    }
 }
