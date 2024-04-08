package com.service.indianfrog.domain.gameroom.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameRoomDto {

    private Long roomId;
    private String roomName;

    public GameRoomDto(Long roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
    }
}
