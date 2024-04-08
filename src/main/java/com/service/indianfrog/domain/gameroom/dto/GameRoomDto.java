package com.service.indianfrog.domain.gameroom.dto;

import lombok.Getter;

@Getter
public class GameRoomDto {

    private Long roomId;
    private String roomName;

    public GameRoomDto(Long roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
    }


    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
