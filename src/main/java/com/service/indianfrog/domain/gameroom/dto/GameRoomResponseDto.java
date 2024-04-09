package com.service.indianfrog.domain.gameroom.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;

import java.time.LocalDateTime;

public class GameRoomResponseDto {

    public record GameRoomCreateResponseDto(
            Long roomId,
            String roomName,

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Seoul/Asia")
            LocalDateTime createdAt
    ){}

    public record GetGameRoomResponseDto(
            Long roomId,
            String roomName
    ){}
}
