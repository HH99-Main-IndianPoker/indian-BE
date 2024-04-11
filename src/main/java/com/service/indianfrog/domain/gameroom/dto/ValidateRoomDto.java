package com.service.indianfrog.domain.gameroom.dto;

import lombok.Getter;

@Getter
public class ValidateRoomDto {
    private Long validId;
    private String participant;
    private boolean host;




    public ValidateRoomDto(Long validId, String participant, boolean host) {
        this.validId = validId;
        this.participant = participant;
        this.host = host;

    }

}
