package com.service.indianfrog.domain.gameroom.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.service.indianfrog.domain.game.entity.GameState;

import java.time.LocalDateTime;
import java.util.List;

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

    public record GetGameRoomResponseDto(
            Long roomId,
            String roomName,
            int participantCount,
            String hostNickname,
            GameState gameState,
            List<ParticipantDto> participants

    ) {
    }
}
