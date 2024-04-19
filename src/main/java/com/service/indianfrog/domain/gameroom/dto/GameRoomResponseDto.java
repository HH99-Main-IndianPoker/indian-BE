package com.service.indianfrog.domain.gameroom.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.service.indianfrog.domain.game.entity.GameState;

import java.time.LocalDateTime;

public class GameRoomResponseDto {

    public record GameRoomCreateResponseDto(
            Long roomId,
            String roomName,
            int participantCount,
            String hostName,
            int myPoint,
            GameState gameState,
            String hostImgUrl,
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Seoul/Asia")
            LocalDateTime createdAt
    ) {
    }

    public record GetAllGameRoomResponseDto(
            Long roomId,
            String roomName,
            int participantCount,
            String hostNickname,
            GameState gameState
    ) {
    }
    public record GetGameRoomResponseDto(
            Long roomId,
            String roomName,
            GameState gameState,
            int participantCount,
            String hostNickname,
            int hostPoints,
            String hostImageUrl,
            String participantNickname,
            int participantPoints,
            String participantImageUrl
    ) {}

}
