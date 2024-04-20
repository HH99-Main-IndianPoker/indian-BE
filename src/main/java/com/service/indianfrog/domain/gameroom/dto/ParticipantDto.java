package com.service.indianfrog.domain.gameroom.dto;

public class ParticipantDto {
    private final String nickname;
    private final int points;
    private final String imageUrl;


    public String getNickname() {
        return nickname;
    }

    public int getPoints() {
        return points;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ParticipantDto(String nickname, int points, String imageUrl) {
        this.nickname = nickname;
        this.points = points;
        this.imageUrl = imageUrl;
    }
}
