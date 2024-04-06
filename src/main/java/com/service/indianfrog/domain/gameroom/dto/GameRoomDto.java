package com.service.indianfrog.domain.gameroom.dto;

import java.util.Set;

public class GameRoomDto {

    private Long roomId;
    private String roomName;
    private Set<String> participants;

    public GameRoomDto(Long roomId, String roomName, Set<String> participants) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.participants = participants;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Set<String> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<String> participants) {
        this.participants = participants;
    }
}
