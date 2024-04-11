package com.service.indianfrog.domain.gameroom.dto;

import lombok.Getter;

@Getter
public class ParticipantInfo {
    private String nickname;
    private int points;
    private boolean host;




    public ParticipantInfo(String nickname, boolean host, int points) {
        this.nickname = nickname;
        this.points=points;
        this.host = host;

    }

}
